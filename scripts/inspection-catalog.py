#!/usr/bin/env python3
import argparse
import csv
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_URL = "https://github.com/JetBrains/intellij-community.git"
KOTLIN = "plugins/kotlin"

REGISTRATIONS = [
    "plugins/kotlin/code-insight/inspections-k2/resources/intellij.kotlin.codeInsight.inspections.xml",
    "plugins/kotlin/code-insight/intentions-k2/resources/intellij.kotlin.codeInsight.intentions.xml",
    "plugins/kotlin/base/scripting/scripting/resources/intellij.kotlin.base.scripting.xml",
    "plugins/kotlin/project-configuration/resources/intellij.kotlin.projectConfiguration.xml",
    "plugins/kotlin/lombok/resources/intellij.kotlin.lombok.xml",
    "plugins/kotlin/i18n/resources/intellij.kotlin.i18n.xml",
    "plugins/kotlin/gradle/scripting/resources/intellij.kotlin.gradle.scripting.xml",
    "plugins/kotlin/gradle/code-insight-groovy/resources/intellij.kotlin.gradle.codeInsight.groovy.xml",
    "plugins/kotlin/maven/resources/intellij.kotlin.maven.xml",
]

SPARSE_PATTERNS = [
    "/plugins/kotlin/**/*.xml",
    "/plugins/kotlin/**/*.properties",
    "/plugins/kotlin/**/*.inspection",
    "/plugins/kotlin/**/*.k2Inspection",
    "/plugins/kotlin/**/inspectionData/inspections.test",
    "/plugins/kotlin/code-insight/inspections-k2/tests/test/**/*Generated.java",
    "/plugins/kotlin/**/src/**/*.kt",
]

GROUPS = {
    "group.names.code.migration": "Code migration",
    "group.names.coroutine": "Coroutines",
    "group.names.java.interop.issues": "Java interop issues",
    "group.names.logging": "Logging",
    "group.names.migration": "Migration",
    "group.names.naming.conventions": "Naming conventions",
    "group.names.numeric.issues": "Numeric issues",
    "group.names.other.problems": "Other problems",
    "group.names.probable.bugs": "Probable bugs",
    "group.names.redundant.constructs": "Redundant constructs",
    "group.names.style.issues": "Style issues",
    "group.names.gradle": "Gradle",
    "group.names.kotlin": "Kotlin",
}

PROJECT = Path(__file__).resolve().parents[1]
RULE_SOURCES = "rules/*/*/src/main/kotlin/**/*.kt"
INSPECTION_ANNOTATION = re.compile(r'@IntellijInspection\("(\w+)"\)\s*class\s+(\w+)')
GROUP_ANNOTATION = re.compile(r'@IntellijInspectionGroup\("([\w.]+)"\)')

NOT_PORTABLE = [
    ("GlobalInspectionTool", "is a global inspection over the whole project"),
    ("ReferencesSearch", "searches references in the project index"),
    ("searchReferences", "searches references in the project index"),
    ("DefinitionsScopedSearch", "searches inheritors in the project index"),
    ("ClassInheritorsSearch", "searches inheritors in the project index"),
    ("OverridingMethodsSearch", "searches overrides in the project index"),
    ("FilenameIndex", "reads the file index"),
    ("KotlinTopLevel", "reads the Kotlin declaration indexes"),
    ("StubIndex", "reads the stub indexes"),
    ("ProjectRootManager", "reads the project roots"),
    ("ProjectFileIndex", "reads the project file index"),
    ("ModuleUtil", "reads the module structure"),
    ("JavaPsiFacade", "resolves Java classes through the project"),
    ("ScriptDefinition", "reads the script definitions of the IDE"),
    ("PropertiesFile", "reads resource bundle files"),
    ("GradleBuildRoot", "reads the Gradle build model"),
    ("MavenDomUtil", "reads the Maven model"),
    ("KotlinFacet", "reads the Kotlin facet settings"),
    ("isInTestSourceContent", "reads the test source roots"),
    ("ExternalSystem", "reads the external build system model"),
]

TYPES = [
    ("KotlinKtDiagnosticBasedInspectionBase", "reports a compiler diagnostic"),
    ("KotlinDiagnosticBasedInspectionBase", "reports a compiler diagnostic"),
    ("diagnostics(", "reads compiler diagnostics"),
    ("resolveToCall", "resolves calls"),
    ("resolveToSymbol", "resolves references to symbols"),
    ("resolveCall", "resolves calls"),
    ("resolveSuccessful", "resolves calls"),
    ("expressionType", "reads expression types"),
    ("expectedType", "reads expected types"),
    ("returnType", "reads declaration types"),
    ("isSubtypeOf", "checks subtyping"),
    ("semanticallyEquals", "compares types"),
    ("symbol", "reads declaration symbols"),
    ("mainReference", "resolves references"),
    (".resolve()", "resolves references"),
    ("languageVersionSettings", "reads the language version of the module"),
    ("apiVersion", "reads the API version of the module"),
    ("CallableId", "matches resolved callables"),
    ("ClassId", "matches resolved classes"),
    ("callableId", "matches resolved callables"),
    ("classId", "matches resolved classes"),
    ("isCalling", "matches resolved callables"),
    ("evaluate(", "evaluates constants"),
    ("builtinTypes", "reads types"),
    ("withValidityAssertion", "runs the Analysis API"),
]

ANALYSIS_API_SIGNATURE = re.compile(r"^\s*(import .*|context\([^)]*KaSession\))\s*$", re.MULTILINE)
ANALYSIS_API_USE = re.compile(r"\bKa(?!Session\b)[A-Z]\w*|\banalyze\s*\(|\bwith\s*\(\s*session\b")


def git(repo, *args):
    return subprocess.run(["git", "-C", str(repo), *args], check=True, capture_output=True, text=True).stdout


def ensure_checkout(repo, ref=None):
    if not (repo / ".git").exists():
        subprocess.run(
            ["git", "clone", "--depth", "1", "--filter=blob:none", "--sparse", REPO_URL, str(repo)],
            check=True,
        )
        git(repo, "sparse-checkout", "set", "--no-cone", *SPARSE_PATTERNS)
    if ref:
        git(repo, "fetch", "--depth", "1", "--filter=blob:none", "origin", ref)
        git(repo, "checkout", "--detach", "FETCH_HEAD")


def rule_sources(project):
    for path in sorted(project.glob(RULE_SOURCES)):
        yield path, path.read_text(encoding="utf-8")


def ported_rules(project):
    rules = {}
    for _, text in rule_sources(project):
        for short_name, rule in INSPECTION_ANNOTATION.findall(text):
            rules.setdefault(short_name, []).append(rule)
    return rules


def group_modules(project):
    modules = {}
    for path, text in rule_sources(project):
        for key in GROUP_ANNOTATION.findall(text):
            parts = path.relative_to(project).parts
            modules[key] = ":" + ":".join(parts[:3])
    return modules


def load_bundles(repo):
    bundles = {}
    for path in (repo / KOTLIN).rglob("*Bundle.properties"):
        if "/resources" not in str(path) or "/testData/" in str(path):
            continue
        name = path.relative_to(path.parents[1]).with_suffix("").as_posix().replace("/", ".")
        entries = bundles.setdefault(name, {})
        for raw in re.sub(r"\\\n\s*", "", path.read_text(encoding="utf-8")).splitlines():
            key, sep, value = raw.partition("=")
            if sep and not raw.lstrip().startswith("#"):
                entries.setdefault(key.strip(), value.strip().replace("''", "'"))
    return bundles


def registrations(repo):
    for xml in REGISTRATIONS:
        if not (repo / xml).is_file():
            print(f"missing registration file {xml}", file=sys.stderr)
            continue
        root = ET.parse(repo / xml).getroot()
        for element in root.iter():
            if element.tag in ("localInspection", "globalInspection"):
                yield xml, element


def source_index(tree):
    index = {}
    for path in tree:
        if path.endswith((".kt", ".java")) and "/testData/" not in path and "/test/" not in path:
            index.setdefault(Path(path).stem, []).append(path)
    return index


ANALYSIS_FUNCTION = re.compile(
    r"(?:context\([^)]*KaSession[^)]*\)\s*(?:@\w+\s*)*(?:[a-z]+\s+)*fun\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?(\w+)"
    r"|fun\s+(?:<[^>]*>\s*)?KaSession\.(\w+))"
)


def sources(repo):
    for path in (repo / KOTLIN).rglob("*.kt"):
        relative = path.relative_to(repo).as_posix()
        if "/testData/" not in relative and "/test/" not in relative:
            yield relative, path.read_text(encoding="utf-8", errors="replace")


def declarations(repo):
    found = {}
    for relative, text in sources(repo):
        for match in re.finditer(r"\bclass\s+(\w+)", text):
            found.setdefault(match.group(1), relative)
    return found


def analysis_functions(repo):
    names = set()
    overrides = set()
    for relative, text in sources(repo):
        overrides.update(re.findall(r"\boverride\s+fun\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?(\w+)", text))
        if "/code-insight/api/" in relative:
            continue
        for match in ANALYSIS_FUNCTION.finditer(text):
            names.add(match.group(1) or match.group(2))
    return names - overrides - {"analyze", "create"}


def implementation_path(fqn, index, declared):
    simple = fqn.rsplit(".", 1)[-1]
    package_dir = fqn.rsplit(".", 1)[0].replace(".", "/")
    candidates = index.get(simple, [])
    exact = [path for path in candidates if f"{package_dir}/{simple}." in path]
    return (exact or candidates or [declared.get(simple, "")])[0]


def k2_test_roots(repo):
    roots = set()
    for generated in (repo / KOTLIN / "code-insight/inspections-k2/tests/test").rglob("*Generated.java"):
        base = generated.parent
        for match in re.finditer(r'@TestMetadata\("([^"]+)"\)', generated.read_text(encoding="utf-8")):
            value = match.group(1)
            if value.endswith((".kt", ".kts", ".java", ".test")):
                continue
            module_root = repo / KOTLIN / "code-insight/inspections-k2/tests"
            roots.add((module_root / value).resolve().relative_to(repo.resolve()).as_posix())
    return roots


def test_data(repo, roots):
    found = {}
    markers = {}
    for path in (repo / KOTLIN).rglob("*"):
        if path.name in (".inspection", ".k2Inspection", "inspections.test") and path.is_file():
            directory = path.parent.parent if path.name == "inspections.test" else path.parent
            markers.setdefault(directory, {})[path.name] = path.read_text(encoding="utf-8")
    for directory, files in markers.items():
        relative = directory.relative_to(repo).as_posix()
        if not any(relative == root or relative.startswith(root + "/") for root in roots):
            continue
        text = files.get(".k2Inspection") or files.get(".inspection") or files.get("inspections.test", "")
        match = re.search(r"([\w.]+Inspection\w*)", text)
        if match:
            found.setdefault(match.group(1), []).append(relative)
    return {fqn: collapse(paths) for fqn, paths in found.items()}


def collapse(paths):
    ordered = sorted(paths)
    kept = []
    for path in ordered:
        if not any(path.startswith(parent + "/") for parent in kept):
            kept.append(path)
    return kept


SUPERTYPES = re.compile(r"(?:class|object)\s+\w+[^{:]*?(?:\([^)]*\))?\s*:\s*([^{]+)")


def supertype_names(clause):
    previous = None
    while previous != clause:
        previous, clause = clause, re.sub(r"\([^()]*\)|<[^<>]*>", "", clause)
    return [match.group(1) for match in re.finditer(r"(?:^|,)\s*(?:\w+\.)*([A-Z]\w*)", clause)]


def with_supertypes(repo, source, simple, declared, depth=3):
    code = own_declaration(source, simple)
    header = SUPERTYPES.search(code)
    if depth == 0 or not header:
        return code
    for supertype in supertype_names(header.group(1)):
        path = declared.get(supertype)
        if path and supertype != simple and "/code-insight/api/" not in path and supertype != "AbstractKotlinInspection":
            parent = (repo / path).read_text(encoding="utf-8", errors="replace")
            code += "\n" + with_supertypes(repo, parent, supertype, declared, depth - 1)
    return code


TOP_LEVEL = re.compile(r"^(?:@\w+(?:\([^)]*\))?\s+)*(?:[a-z]+ )*(class|object|interface|fun|val|var|typealias)\b\s*(?:<[^>]*>\s*)?(\w+)?", re.MULTILINE)


def own_declaration(source, simple):
    starts = [(match.start(), match.group(1), match.group(2)) for match in TOP_LEVEL.finditer(source)]
    classes = [name for _, kind, name in starts if kind in ("class", "object", "interface")]
    if len(classes) < 2 or simple not in classes:
        return source
    kept = []
    for index, (start, kind, name) in enumerate(starts):
        end = starts[index + 1][0] if index + 1 < len(starts) else len(source)
        if kind not in ("class", "object", "interface") or name == simple:
            kept.append(source[start:end])
    return "\n".join(kept)


def local_search(code):
    searches = re.findall(r"ReferencesSearch\s*\.search\(([^\n]*)", code)
    return bool(searches) and all("LocalSearchScope" in arguments for arguments in searches)


def classify(source, module, language, helpers=frozenset()):
    if language.lower() not in ("kotlin", ""):
        return "not-portable", f"inspects {language} files"
    if not source:
        return "not-portable", "implementation not found"
    code = ANALYSIS_API_SIGNATURE.sub("", source)
    for token, reason in NOT_PORTABLE:
        if token in code and not (token == "ReferencesSearch" and local_search(code)):
            return "not-portable", reason
    if "ReferencesSearch" in code:
        return "types", "searches references in one file"
    for token, reason in TYPES:
        if token in code:
            return "types", reason
    use = ANALYSIS_API_USE.search(code)
    if use:
        return "types", f"uses the Analysis API (`{use.group(0).strip()}`)"
    for call in re.finditer(r"\b(\w+)\s*\(", code):
        if call.group(1) in helpers:
            return "types", f"calls `{call.group(1)}`, which uses the Analysis API"
    return "psi", "uses only the PSI of one file"


def scope(group_path, group_key, module, language):
    if "React" in group_path:
        return "out: React"
    if language.lower() in ("xml", "groovy") or group_key == "group.names.gradle":
        return "out: build file"
    if not module:
        return "out: no matching group"
    return "in"


def load_overrides(path):
    if not path.exists():
        return {}
    with path.open(newline="", encoding="utf-8") as handle:
        return {row["shortName"]: row for row in csv.DictReader(handle)}


def build(repo, declared, modules):
    tree = git(repo, "ls-tree", "-r", "--name-only", "HEAD", KOTLIN).splitlines()
    index = source_index(tree)
    bundles = load_bundles(repo)
    rows = []
    for xml, element in registrations(repo):
        attrs = element.attrib
        fqn = attrs.get("implementationClass", "")
        simple = fqn.rsplit(".", 1)[-1]
        short_name = attrs.get("shortName") or re.sub(r"Inspection$", "", simple)
        bundle = attrs.get("bundle") or attrs.get("groupBundle") or "messages.KotlinBundle"
        name = attrs.get("displayName") or bundles.get(bundle, {}).get(attrs.get("key", ""), attrs.get("key", ""))
        group_key = attrs.get("groupKey", "")
        group_name = GROUPS.get(group_key, attrs.get("groupName", ""))
        module = modules.get(group_key, "")
        group_path = attrs.get("groupPath", "Kotlin")
        if "React" in group_path:
            group_name, module = "React", ""
        rows.append({
            "shortName": short_name,
            "displayName": name,
            "group": group_name,
            "module": module,
            "scope": scope(group_path, attrs.get("groupKey", ""), module, attrs.get("language", "")),
            "level": attrs.get("level", "WARNING"),
            "enabledByDefault": attrs.get("enabledByDefault", "false"),
            "implementationClass": fqn,
            "implementationPath": implementation_path(fqn, index, declared),
            "language": attrs.get("language", ""),
            "registration": xml,
        })
    return rows


def fetch_sources(repo, rows):
    paths = [f"/{row['implementationPath']}" for row in rows if row["implementationPath"]]
    git(repo, "sparse-checkout", "add", *paths)


def write_csv(path, rows, columns):
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def write_markdown(path, rows, commit):
    modules = sorted({row["module"] for row in rows if row["module"]})
    verdicts = ["psi", "types", "not-portable"]
    lines = [
        "# IntelliJ Kotlin inspections",
        "",
        "The backlog of the Kotlin inspections of IntelliJ IDEA to port to detekt rules.",
        f"Source: [JetBrains/intellij-community@{commit[:10]}](https://github.com/JetBrains/intellij-community/tree/{commit}).",
        "The machine-readable copy is [inspections.csv](inspections.csv).",
        "",
        "Refresh with `scripts/inspection-catalog.py <intellij-community checkout>`. Put manual verdicts",
        "into [inspection-overrides.csv](inspection-overrides.csv), because the script finds the",
        "verdicts with heuristics. The `ported` status comes from the `@IntellijInspection` annotations",
        "of the rules.",
        "",
        "Verdicts:",
        "",
        "- `psi`: a plain detekt rule can do it with the PSI of one file.",
        "- `types`: the rule needs type resolution, i.e. `RequiresAnalysisApi` and the Kotlin Analysis API.",
        "- `not-portable`: the inspection needs the project, a module, a build file, an index or IDE state.",
        "",
        "Status: `ported` names the rule of this project, `covered` names the detekt or ktlint rule that",
        "already finds the same problem.",
        "",
        "## Summary",
        "",
        "| Module | " + " | ".join(f"`{verdict}`" for verdict in verdicts) + " | Total | Ported | Covered |",
        "|---|" + "---:|" * (len(verdicts) + 3),
    ]
    for module in modules + [""]:
        group = [row for row in rows if row["module"] == module]
        counts = [sum(row["verdict"] == verdict for row in group) for verdict in verdicts]
        label = f"`{module}`" if module else "Out of scope"
        done = [sum(row["status"].startswith(state) for row in group) for state in ("ported", "covered")]
        lines.append(f"| {label} | " + " | ".join(map(str, counts + [len(group)] + done)) + " |")
    totals = [sum(row["verdict"] == verdict for row in rows) for verdict in verdicts]
    done = [sum(row["status"].startswith(state) for row in rows) for state in ("ported", "covered")]
    lines.append("| **All** | " + " | ".join(map(str, totals + [len(rows)] + done)) + " |")
    for module in modules + [""]:
        group = sorted((row for row in rows if row["module"] == module), key=lambda row: row["shortName"])
        lines += ["", f"## `{module}`" if module else "## Out of scope", ""]
        lines.append("| Inspection | Level | Verdict | Reason | Status | Implementation | Test data |")
        lines.append("|---|---|---|---|---|---|---|")
        for row in group:
            source = link(commit, row["implementationPath"], Path(row["implementationPath"]).name)
            tests = "<br>".join(link(commit, path, path.removeprefix(KOTLIN + "/")) for path in row["testData"].split(";") if path)
            title = row["displayName"].replace("|", "\\|")
            reason = row["reason"] if row["scope"] == "in" else f"{row['scope']}; {row['reason']}"
            lines.append(
                f"| `{row['shortName']}`<br>{title} | {row['level']} | `{row['verdict']}` | {reason} | {row['status']} | {source} | {tests} |"
            )
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def link(commit, path, text):
    if not path:
        return ""
    return f"[{text}](https://github.com/JetBrains/intellij-community/blob/{commit}/{path})"


COLUMNS = [
    "shortName", "displayName", "group", "module", "scope", "level", "enabledByDefault", "verdict", "reason", "status",
    "implementationClass", "implementationPath", "testData", "language", "registration",
]


def catalog(repo, docs, ref=None):
    ensure_checkout(repo, ref)
    commit = git(repo, "rev-parse", "HEAD").strip()
    modules = group_modules(PROJECT)
    fetch_sources(repo, build(repo, {}, modules))
    declared = declarations(repo)
    rows = build(repo, declared, modules)
    helpers = analysis_functions(repo)
    tests = test_data(repo, k2_test_roots(repo))
    overrides = load_overrides(docs / "inspection-overrides.csv")
    ported = ported_rules(PROJECT)
    for row in rows:
        path = repo / row["implementationPath"] if row["implementationPath"] else None
        source = path.read_text(encoding="utf-8") if path and path.is_file() else ""
        source = with_supertypes(repo, source, row["implementationClass"].rsplit(".", 1)[-1], declared)
        row["verdict"], row["reason"] = classify(source, row["module"], row["language"], helpers)
        if row["scope"] != "in":
            row["verdict"] = "not-portable"
        override = overrides.get(row["shortName"], {})
        row["verdict"] = override.get("verdict") or row["verdict"]
        row["reason"] = override.get("reason") or row["reason"]
        rules = ported.get(row["shortName"])
        row["status"] = "ported: " + ", ".join(f"`{rule}`" for rule in rules) if rules else override.get("status") or ""
        row["testData"] = ";".join(tests.get(row["implementationClass"], []))
    rows.sort(key=lambda row: (row["module"] or "~", row["shortName"]))
    return rows, commit


def main():
    parser = argparse.ArgumentParser(description="Catalogs the Kotlin inspections of IntelliJ IDEA.")
    parser.add_argument("checkout", type=Path, help="intellij-community checkout; a sparse clone is made if absent")
    parser.add_argument("--docs", type=Path, default=PROJECT / "docs")
    parser.add_argument("--ref", help="intellij-community branch, tag or commit to check out")
    args = parser.parse_args()
    rows, commit = catalog(args.checkout.resolve(), args.docs, args.ref)
    args.docs.mkdir(parents=True, exist_ok=True)
    write_csv(args.docs / "inspections.csv", rows, COLUMNS)
    write_markdown(args.docs / "inspections.md", rows, commit)
    print(f"{len(rows)} inspections from {commit}", file=sys.stderr)

if __name__ == "__main__":
    main()
