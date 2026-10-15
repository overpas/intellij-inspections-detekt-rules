---
name: port-inspections
description: Principles for porting a group of IntelliJ inspections to detekt rules. Invoke when the user asks to port, add or review the rules of an inspection group. Use it together with the code-conventions skill.
---

# Port inspections

An IDE inspection is a hint next to the caret. A detekt rule adds a finding to a report or fails a
build. Port an inspection when its rule helps a user in CI, and use judgment in each case.

1. **Follow upstream defaults.** If IntelliJ turns an inspection off by default, make its rule
   inactive by default.
2. **Mark opposite rules.** If two rules ask for opposite changes, mark them as opposites. Make only
   one of them active by default: the one that is more idiomatic in Kotlin.
3. **Question the new rules.** After the port, find the rules that make little sense as lint rules,
   for example refactoring hints, noisy heuristics or duplicates of detekt rules. Tell the user, with
   a recommendation, and let the user decide.
4. **Explain what is not ported.** Give a short reason for each inspection that is not ported, for
   example that the compiler or detekt already reports it, or that detekt does not have the data.
   Give it to the user, not in the README, which documents only the ported rules.
