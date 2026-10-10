#!/usr/bin/env python3
import argparse
import csv
import importlib.util
import re
import sys
from pathlib import Path

PROJECT = Path(__file__).resolve().parents[1]
DOCS = PROJECT / "docs"
UPSTREAM = "https://github.com/JetBrains/intellij-community"
REGISTRATION = re.compile(r"<(?:local|global)Inspection\b")
ISSUE_LIMIT = 60000
ATTRIBUTES = ["displayName", "group", "level", "enabledByDefault", "implementationClass"]


def load_catalog():
    spec = importlib.util.spec_from_file_location("inspection_catalog", PROJECT / "scripts" / "inspection-catalog.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def baseline_commit():
    match = re.search(r"/tree/([0-9a-f]{40})", (DOCS / "inspections.md").read_text(encoding="utf-8"))
    if not match:
        sys.exit("docs/inspections.md does not name an intellij-community commit")
    return match.group(1)


def baseline_rows():
    with (DOCS / "inspections.csv").open(newline="", encoding="utf-8") as handle:
        return {row["shortName"]: row for row in csv.DictReader(handle)}


def changed_files(catalog, repo, base, head):
    catalog.git(repo, "fetch", "--depth", "1", "--filter=blob:none", "origin", base)
    return catalog.git(repo, "diff", "--name-only", base, head, "--", catalog.KOTLIN).splitlines()


def registration_files(catalog, repo):
    missing = [xml for xml in catalog.REGISTRATIONS if not (repo / xml).is_file()]
    unknown = sorted(
        path.relative_to(repo).as_posix() for path in (repo / catalog.KOTLIN).rglob("*.xml")
        if "/testData/" not in path.as_posix() and path.relative_to(repo).as_posix() not in catalog.REGISTRATIONS
        and REGISTRATION.search(path.read_text(encoding="utf-8", errors="replace"))
    )
    return missing, unknown


def touched(paths, files):
    return any(file == path or file.startswith(path + "/") for path in paths if path for file in files)


def rule_changes(old, new, files):
    changes = [f"{name}: {old[name] or '-'} → {new[name] or '-'}" for name in ATTRIBUTES if old[name] != new[name]]
    if old["implementationPath"] != new["implementationPath"]:
        changes.append("implementation moved")
    elif touched([old["implementationPath"]], files):
        changes.append("implementation changed")
    if touched(old["testData"].split(";"), files):
        changes.append("test data changed")
    return changes


def history(commit, path):
    return f"[history]({UPSTREAM}/commits/{commit}/{path})" if path else ""


def report(old, new, ported, files, registrations, base, head):
    added = sorted(set(new) - set(old))
    removed = sorted(set(old) - set(new))
    replacements = {new[name]["implementationClass"]: name for name in added}
    changed = []
    for name in sorted(set(old) & set(new)):
        if new[name]["status"].startswith("ported"):
            changes = rule_changes(old[name], new[name], files)
            if changes:
                changed.append((name, changes))
    missing, unknown = registrations
    if not (added or removed or changed or missing or unknown):
        return ""
    lines = [
        f"Upstream changes of the Kotlin inspections in [{base[:10]}...{head[:10]}]({UPSTREAM}/compare/{base}...{head}).",
        "",
        "To accept them, port or classify the inspections, then refresh the catalog with",
        "`scripts/inspection-catalog.py <intellij-community checkout> --ref master`. The refresh moves the baseline.",
    ]
    if missing or unknown:
        lines += ["", "## Registration files", "", "Update `REGISTRATIONS` in `scripts/inspection-catalog.py`.", ""]
        lines += [f"- missing: `{xml}`" for xml in missing]
        lines += [f"- not cataloged: [{xml}]({UPSTREAM}/blob/{head}/{xml})" for xml in unknown]
    if added:
        lines += ["", f"## New inspections ({len(added)})", "", "| Inspection | Group | Rule set | Level | Verdict | Source |", "|---|---|---|---|---|---|"]
        for name in added:
            row = new[name]
            module = f"`{row['module']}`" if row["module"] else row["scope"]
            source = f"[{Path(row['implementationPath']).name}]({UPSTREAM}/blob/{head}/{row['implementationPath']})" if row["implementationPath"] else ""
            lines.append(f"| `{name}`<br>{row['displayName']} | {row['group']} | {module} | {row['level']} | `{row['verdict']}` | {source} |")
    if removed:
        lines += ["", f"## Removed inspections ({len(removed)})", "", "| Inspection | Status | Replaced by |", "|---|---|---|"]
        for name in removed:
            row = old[name]
            replacement = replacements.get(row["implementationClass"], "")
            rules = ", ".join(f"`{rule}`" for rule in ported.get(name, []))
            status = f"ported: {rules}" if rules else row["status"] or row["verdict"]
            lines.append(f"| `{name}` | {status} | {f'`{replacement}`' if replacement else ''} |")
    if changed:
        lines += ["", f"## Changed rules ({len(changed)})", "", "| Rule | Rule set | Changes | Upstream |", "|---|---|---|---|"]
        size = sum(len(line) + 1 for line in lines)
        for index, (name, changes) in enumerate(changed):
            row = old[name]
            line = f"| `{name}` | `{row['module']}` | {'<br>'.join(changes)} | {history(head, new[name]['implementationPath'])} |"
            if size + len(line) > ISSUE_LIMIT:
                lines += ["", f"{len(changed) - index} more changed rules. Run `scripts/inspection-sync.py` for the full list."]
                break
            lines.append(line)
            size += len(line) + 1
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description="Reports the upstream changes of the cataloged IntelliJ Kotlin inspections.")
    parser.add_argument("checkout", type=Path, help="intellij-community checkout; a sparse clone is made if absent")
    parser.add_argument("--ref", default="master", help="intellij-community branch, tag or commit to compare with")
    parser.add_argument("--output", type=Path, required=True, help="file for the Markdown report; empty if nothing changed")
    args = parser.parse_args()
    catalog = load_catalog()
    repo = args.checkout.resolve()
    rows, head = catalog.catalog(repo, DOCS, args.ref)
    base = baseline_commit()
    files = changed_files(catalog, repo, base, head)
    registrations = registration_files(catalog, repo)
    new = {row["shortName"]: row for row in rows}
    body = report(baseline_rows(), new, catalog.ported_rules(catalog.PROJECT), files, registrations, base, head)
    args.output.write_text(body, encoding="utf-8")
    print(f"{len(files)} changed files in {catalog.KOTLIN} between {base[:10]} and {head[:10]}", file=sys.stderr)


if __name__ == "__main__":
    main()
