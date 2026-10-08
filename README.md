# intellij-inspections-detekt-rules

[![Build](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml/badge.svg)](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml)

[detekt](https://detekt.dev) rules ported from the Kotlin inspections of IntelliJ IDEA. They give
the same hints outside the IDE, e.g. to AI agents and CI.

Each module is one inspection group of the IDE settings (Editor | Inspections | Kotlin).

| Module | Rule set id | Jar |
|---|---|---|
| `rules:kotlin:code-migration` | `intellij-kotlin-code-migration` | `intellij-inspections-kotlin-code-migration-<version>.jar` |
| `rules:kotlin:coroutines` | `intellij-kotlin-coroutines` | `intellij-inspections-kotlin-coroutines-<version>.jar` |
| `rules:kotlin:java-interop` | `intellij-kotlin-java-interop` | `intellij-inspections-kotlin-java-interop-<version>.jar` |
| `rules:kotlin:logging` | `intellij-kotlin-logging` | `intellij-inspections-kotlin-logging-<version>.jar` |
| `rules:kotlin:migration` | `intellij-kotlin-migration` | `intellij-inspections-kotlin-migration-<version>.jar` |
| `rules:kotlin:naming-conventions` | `intellij-kotlin-naming-conventions` | `intellij-inspections-kotlin-naming-conventions-<version>.jar` |
| `rules:kotlin:numeric-issues` | `intellij-kotlin-numeric-issues` | `intellij-inspections-kotlin-numeric-issues-<version>.jar` |
| `rules:kotlin:other-problems` | `intellij-kotlin-other-problems` | `intellij-inspections-kotlin-other-problems-<version>.jar` |
| `rules:kotlin:probable-bugs` | `intellij-kotlin-probable-bugs` | `intellij-inspections-kotlin-probable-bugs-<version>.jar` |
| `rules:kotlin:redundant-constructs` | `intellij-kotlin-redundant-constructs` | `intellij-inspections-kotlin-redundant-constructs-<version>.jar` |
| `rules:kotlin:style-issues` | `intellij-kotlin-style-issues` | `intellij-inspections-kotlin-style-issues-<version>.jar` |

## Backlog

[docs/inspections.md](docs/inspections.md) lists every Kotlin inspection of IntelliJ IDEA with its
target module and a verdict on how to port it. Refresh it with:

```shell
python3 scripts/inspection-catalog.py <intellij-community checkout>
```

The script makes a sparse clone at the given path if it does not exist.

## Rules

Every rule takes the standard detekt options (`active`, `excludes`, `includes`, ...). The rule
names are the short names of the IntelliJ inspections.

### `intellij-kotlin-redundant-constructs`

| Rule | Finds |
|---|---|
| `RedundantGetter` | A getter that only returns the backing field. |
| `RedundantLabeledReturnOnLastExpressionInLambda` | A `return@label` on the last expression of the lambda with that label. |
| `RedundantLambdaOrAnonymousFunction` | A lambda or an anonymous function that is called at once. |
| `RemoveSetterParameterType` | An explicit type on a setter parameter. |
| `SimplifyWhenWithBooleanConstantCondition` | A subjectless `when` with a `true` or `false` branch condition. |
| `WhenWithOnlyElse` | A `when` with only an `else` branch. |

## Build

```shell
./gradlew build      # compile, test, detekt
./gradlew koverVerify
./gradlew ruleJars   # collects the versioned jars into build/releases/<version>/
```

## Use in a project

Download the jars of a release from
[GitHub Releases](https://github.com/overpas/intellij-inspections-detekt-rules/releases) into the
project, e.g. `config/detekt/plugins/`, and add them to the detekt plugins:

```kotlin
dependencies {
    detektPlugins(fileTree(rootProject.layout.projectDirectory.dir("config/detekt/plugins")) { include("*.jar") })
}
```

## License

Apache License 2.0. The rules are adapted from
[IntelliJ IDEA Community Edition](https://github.com/JetBrains/intellij-community), see
[NOTICE](NOTICE).
