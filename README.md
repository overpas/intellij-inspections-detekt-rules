# intellij-inspections-detekt-rules

[![Build](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml/badge.svg)](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml)

[detekt](https://detekt.dev) rules ported from the Kotlin inspections of IntelliJ IDEA. They give
the same hints outside the IDE, e.g. to AI agents and CI.

Each module is one inspection group of the IDE settings (Editor | Inspections | Kotlin).

| Module | Rule set id | Jar |
|---|---|---|
| `rules:code-migration` | `intellij-code-migration` | `intellij-inspections-code-migration-<version>.jar` |
| `rules:coroutines` | `intellij-coroutines` | `intellij-inspections-coroutines-<version>.jar` |
| `rules:java-interop` | `intellij-java-interop` | `intellij-inspections-java-interop-<version>.jar` |
| `rules:logging` | `intellij-logging` | `intellij-inspections-logging-<version>.jar` |
| `rules:migration` | `intellij-migration` | `intellij-inspections-migration-<version>.jar` |
| `rules:naming-conventions` | `intellij-naming-conventions` | `intellij-inspections-naming-conventions-<version>.jar` |
| `rules:numeric-issues` | `intellij-numeric-issues` | `intellij-inspections-numeric-issues-<version>.jar` |
| `rules:other-problems` | `intellij-other-problems` | `intellij-inspections-other-problems-<version>.jar` |
| `rules:probable-bugs` | `intellij-probable-bugs` | `intellij-inspections-probable-bugs-<version>.jar` |
| `rules:redundant-constructs` | `intellij-redundant-constructs` | `intellij-inspections-redundant-constructs-<version>.jar` |
| `rules:style-issues` | `intellij-style-issues` | `intellij-inspections-style-issues-<version>.jar` |

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
