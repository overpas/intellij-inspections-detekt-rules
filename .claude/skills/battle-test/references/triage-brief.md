# Triage brief

Copy this file to `<work>/analysis/BRIEF.md` and replace `<repo>`, `<work>` and the project list.

---

Goal: classify the findings of the detekt rules that port IntelliJ IDEA inspections.

- Rules repo: `<repo>`. Rule sources are in `rules/<kind>/<group>/src/main/kotlin`, tests in
  `src/test`, the default config in `src/main/resources/config/config.yml`.
- The rules ran with all rules active on these projects, cloned under `<work>/projects`:
  `<project list with pinned commits>`. Generated code is already filtered out.

Inputs:

- `<work>/analysis/group-<G>.txt`: your rules.
- `<work>/analysis/counts.csv`: findings per rule and project, and whether the rule is active by
  default.
- `<work>/analysis/samples/<Rule>.md`: up to 14 sampled findings per rule with code context. `>`
  marks the reported lines. "Same line detekt" lists detekt core rules on the same line.
- `<work>/analysis/findings.json`: all findings, if you need more samples.
- The real source files in `<work>/projects`, when the context is not enough.

Do not edit the rules repo. Do not edit the projects, except to prove a finding: apply the
suggested change, compile the module, then revert only that file with
`git checkout -- <file>`. Other agents and the build patches of the harness share the clone.

Put each rule in exactly one category:

1. BUG: a crash, a wrong location, a wrong message, a suggested fix that does not compile or that
   changes behavior, or behavior that clearly differs from the IntelliJ inspection.
2. FALSE_POSITIVE: a finding that IntelliJ IDEA does not highlight, or that is plainly wrong for
   this code.
3. QUESTIONABLE_STYLE: the rule works as designed, but on real code most findings do not make the
   code more readable, so a team would turn it off. Only for rules that are active by default.
4. OK: true positives, worth a fix.

Verify each BUG and FALSE_POSITIVE against the rule source and the real code. Give the root cause
in the rule (`file:line`) when you can. Do not guess what IntelliJ does: if you are not sure, say
"unsure" and why. Count the sampled findings in each category, so that the share of false
positives is visible.

Write the full report to `<work>/analysis/triage-<G>.md`. For each rule that is not OK:

- The rule id, the category and the number of affected findings (sampled, and the estimate of the
  total).
- 1-2 real code snippets (`path:line`) with the finding.
- BUG or FALSE_POSITIVE: why it is wrong, the root cause in the rule source, a minimal Kotlin
  reproducer for a unit test and the expected behavior.
- QUESTIONABLE_STYLE: the code before and the code after the change that the rule asks for, and
  one argument for and one against.

List the OK rules in one line at the end.

The final message: a short summary of at most 40 lines. Give the rules that are not OK with the
category, a one-line reason and the share of false positives. The full detail stays in the file.
