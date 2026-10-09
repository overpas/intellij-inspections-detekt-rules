# Coding

## General

- Don't edit the code that doesn't affect the task you are working on.
- Keep visibility of interfaces, classes, functions, properties minimal.
- In each class, object and file, keep the helper functions no more than the entry points. Put extra
  helpers behind a new abstraction, because a warning does not fail the build.
- Keep each class and object one group of linked members. Split a class whose members fall into
  unrelated groups, because each group is a separate responsibility.

## Rules

- Annotate each rule with the inspection annotation and the short name of its IntelliJ inspection,
  and each rule set provider with the group annotation. The catalog and the upstream sync scripts
  read them.

## Kotlin

When writing kotlin code use skills:

- /kotlin-control-flow
- /kotlin-api-design
