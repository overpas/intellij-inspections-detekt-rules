import argparse
import collections
import csv
import json
import random
import re
from pathlib import Path
from urllib.parse import unquote, urlparse

GENERATED = ("/gen/", "/generated/")
SAMPLE_SIZE = 14
CONTEXT = 3


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", required=True, type=Path)
    parser.add_argument("--out", required=True, type=Path)
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("projects", nargs="+", metavar="NAME=PATH")
    args = parser.parse_args()
    projects = collections.defaultdict(list)
    for project in args.projects:
        name, path = project.split("=", 1)
        projects[name].append(Path(path).resolve())
    return args, projects


def default_active(repo):
    active = {}
    for yml in repo.glob("rules/*/*/src/main/resources/config/config.yml"):
        rule_set = rule = None
        for line in yml.read_text().splitlines():
            if m := re.match(r"^([\w-]+):$", line):
                rule_set = m.group(1)
            elif m := re.match(r"^  (\w+):$", line):
                rule = m.group(1)
            elif (m := re.match(r"^    active: (\w+)", line)) and rule:
                active[f"{rule_set}.{rule}"] = m.group(1) == "true"
    return active


def to_path(uri):
    return Path(unquote(urlparse(uri).path)) if uri.startswith("file:") else Path(unquote(uri))


def load(projects):
    seen = set()
    findings = []
    for name, roots in projects.items():
        for root in roots:
            for sarif in root.glob("**/build/reports/detekt/*.sarif"):
                if "/build-logic/" in str(sarif):
                    continue
                for run in json.loads(sarif.read_text()).get("runs", []):
                    base = run.get("originalUriBaseIds", {}).get("%SRCROOT%", {}).get("uri")
                    for result in run.get("results", []):
                        location = result["locations"][0]["physicalLocation"]
                        path = to_path(location["artifactLocation"]["uri"])
                        if not path.is_absolute():
                            path = (to_path(base) if base else root) / path
                        if any(part in str(path) for part in GENERATED):
                            continue
                        region = location.get("region", {})
                        rule = result["ruleId"].removeprefix("detekt.")
                        key = (rule, str(path), region.get("startLine"), region.get("startColumn"))
                        if key in seen:
                            continue
                        seen.add(key)
                        findings.append({
                            "project": name,
                            "rule": rule,
                            "file": str(path),
                            "line": region.get("startLine", 0),
                            "col": region.get("startColumn", 0),
                            "endLine": region.get("endLine", region.get("startLine", 0)),
                            "message": result["message"]["text"],
                        })
    return findings


def context(finding):
    try:
        lines = Path(finding["file"]).read_text(errors="replace").splitlines()
    except OSError:
        return "<missing>"
    start = max(0, finding["line"] - 1 - CONTEXT)
    end = min(len(lines), finding["endLine"] + CONTEXT, start + 2 * CONTEXT + 12)
    out = []
    for number in range(start, end):
        marker = ">" if finding["line"] - 1 <= number <= finding["endLine"] - 1 else " "
        out.append(f"{marker}{number + 1:5d} {lines[number]}")
    return "\n".join(out)


def write_samples(out, ours, core, counts, active, names):
    samples = out / "samples"
    samples.mkdir(exist_ok=True)
    by_line = collections.defaultdict(set)
    for finding in core:
        by_line[(finding["file"], finding["line"])].add(finding["rule"])
    grouped = collections.defaultdict(lambda: collections.defaultdict(list))
    for finding in ours:
        grouped[finding["rule"]][finding["project"]].append(finding)
    rng = random.Random(7)
    for rule, per_project in grouped.items():
        for bucket in per_project.values():
            rng.shuffle(bucket)
        chosen = []
        while len(chosen) < SAMPLE_SIZE and any(per_project.values()):
            for name in names:
                if per_project.get(name) and len(chosen) < SAMPLE_SIZE:
                    chosen.append(per_project[name].pop())
        total = sum(counts[rule].values())
        lines = [f"# {rule} (default active: {active.get(rule, '?')}; {total} findings: "
                 + ", ".join(f"{n}={counts[rule][n]}" for n in names) + ")\n"]
        for finding in chosen:
            lines.append(f"## {finding['project']} {finding['file']}:{finding['line']}:{finding['col']}")
            lines.append(f"Message: {finding['message']}")
            if same_line := by_line.get((finding["file"], finding["line"])):
                lines.append(f"Same line detekt: {', '.join(sorted(same_line))}")
            lines.append("```kotlin\n" + context(finding) + "\n```\n")
        (samples / f"{rule.split('.')[-1]}.md").write_text("\n".join(lines))


def write_counts(out, counts, active, names, baseline):
    old = {}
    if baseline:
        old = {row["rule"]: int(row["total"]) for row in csv.DictReader(baseline.open())}
    with open(out / "counts.csv", "w", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["rule", "default_active", "total", *names, *(["baseline", "delta"] if baseline else [])])
        for rule in sorted(set(counts) | set(old), key=lambda r: -sum(counts[r].values())):
            total = sum(counts[rule].values())
            delta = [old.get(rule, 0), total - old.get(rule, 0)] if baseline else []
            writer.writerow([rule, active.get(rule, "?"), total, *[counts[rule][n] for n in names], *delta])


def main():
    args, projects = parse_args()
    args.out.mkdir(parents=True, exist_ok=True)
    active = default_active(args.repo)
    findings = load(projects)
    ours = [f for f in findings if f["rule"].startswith("intellij-")]
    core = [f for f in findings if not f["rule"].startswith("intellij-")]
    counts = collections.defaultdict(collections.Counter)
    for finding in ours:
        counts[finding["rule"]][finding["project"]] += 1
    names = list(projects)
    rules = len(counts)
    active_total = sum(sum(c.values()) for rule, c in counts.items() if active.get(rule))
    write_counts(args.out, counts, active, names, args.baseline)
    write_samples(args.out, ours, core, counts, active, names)
    (args.out / "findings.json").write_text(json.dumps(ours))
    print(f"findings={len(ours)} rules={rules} default_active_findings={active_total}")


main()
