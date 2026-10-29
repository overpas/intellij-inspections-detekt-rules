---
name: battle-test
description: Runs the rules on real open-source Kotlin projects, triages the findings and dispatches the fixes. Invoke when the user asks to battle test the rules, to run them on real projects, or to re-check the projects of an earlier battle test.
argument-hint: "<git URLs of the projects>"
---

# Battle test

A battle test runs the rules from the current `develop` build on real projects. It finds crashes,
false positives and rules that real code does not need. Use it together with the code-conventions
skill. The `scripts/` and `references/` paths are relative to the directory of this skill.

## Setup

1. Get the project URLs from the arguments. If there are none, ask the user.
2. Make the work directory `<work>` = `~/.cache/intellij-inspections-battle/<date>`. Do not use
   the scratchpad: the system deletes it overnight, with the clones and the baseline counts.
3. Clone each project into `<work>/projects` with `--recurse-submodules`. Write the name, URL and
   `git rev-parse HEAD` of each clone to `<work>/projects.txt`. A re-check must analyze the same
   commits.
4. Run `./gradlew ruleJars` in this repo. Copy the bundle jars, and the rule set jars that no bundle
   contains, from `build/releases/<version>/` to `<work>/jars`. Do not copy both a bundle and its
   rule sets, or each finding shows two times.

## Run

1. Prepare each project:
    - Add `id("dev.detekt") version "<detekt version of this repo>" apply false` to the `plugins`
      block of the root build script. Detekt must share the class loader of the Kotlin Gradle
      plugin. If the root build uses a `buildscript` classpath, add
      `dev.detekt:detekt-gradle-plugin:<version>` there instead.
    - If the project already applies detekt, set its version to the detekt version of this repo.
      Remove its own `tasks.withType<Detekt>` settings that turn off SARIF, set `parallel` or call
      `setSource`, because they override the init script.
    - Write `sdk.dir=<ANDROID_HOME>` to `local.properties`.
2. Do a dry run: `scripts/run.sh <work> <project dir> dry-<name> battleDetekt --dry-run`. Make sure
   that the task list has only type-resolution tasks: one debug variant per Android module and the
   `*Jvm` tasks of multiplatform modules. Fix configuration errors in the log first. A missing
   native or generated dependency can need a stub jar, for example in `app/libs`.
3. Run `scripts/run.sh <work> <project dir> <name> battleDetekt` in the background, one project at
   a time. A large project takes more than 10 minutes.
4. Read the log. A rule exception stops the detekt task and the task writes no report. Each crash
   is a BUG.

## Triage

1. Run `python3 -I scripts/analyze.py --repo <this repo> --out <work>/analysis <name>=<project dir>...`.
   Give a name more than one time when a project has more than one root build. The script filters
   generated code and writes `counts.csv`, `samples/<Rule>.md` and `findings.json`.
2. Split the rules with findings into groups of about 25 rules, by rule set. Write each group to
   `<work>/analysis/group-<G>.txt`.
3. Fill in the brief from `references/triage-brief.md`. Start one background subagent for each
   group, all in one message: "Read and follow `<work>/analysis/BRIEF.md`. Your group is `<G>`."
4. Verify each BUG and FALSE_POSITIVE claim yourself against the rule source before you act on it.

## Report

1. **BUG and FALSE_POSITIVE**: start one session for each root cause or rule set, right away, in
   its own worktree from `develop`. Two sessions must not change the same files. Each brief has:
    - The background: the battle test found it, invoke the code-conventions skill, open a PR into
      `develop`, run `./gradlew --stop` before the final verification.
    - The rule source path, the real code snippet, the root cause and "done when" with the tests to
      add.
2. **QUESTIONABLE_STYLE**: publish an artifact. For each rule, show the finding count, the real
   code before and after the change, one argument for and one against, and the choices, for example
   "Keep as is", "Turn off by default", "Add an option" or "Skip a case". Let the user decide, then
   apply all decisions in one PR.
3. Report the OK rules in one line, and the projects with their pinned commits.
4. Copy `counts.csv` to `<work>/baseline.csv`.

## Re-check

Do this when the user asks to re-check, for example after the fix PRs are merged or released.

1. Run `./gradlew ruleJars` again and replace the jars in `<work>/jars`. Run `./gradlew --stop` in
   each project: the daemon keeps the old rule classes.
2. Check that each clone is still at the commit in `<work>/projects.txt`, and run the projects
   again.
3. Run `analyze.py` with `--baseline <work>/baseline.csv` and `--out <work>/recheck`. Report the
   rules whose counts did not change as the fixes expect, and triage the new findings.
