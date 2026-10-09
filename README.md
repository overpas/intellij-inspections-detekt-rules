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
| `CanBeParameter` | A `val` or `var` constructor parameter that is only used during initialization. Only parameters that are not visible outside the file are checked. |
| `CanBePrimaryConstructorProperty` | A property that is initialized from the constructor parameter of the same name and type. |
| `CanUnescapeDollarLiteral` | An escaped dollar in a string literal that can be a plain `$`. |
| `ConstantConditionIf` | An `if` whose condition is the constant `true` or `false`. |
| `ExplicitThis` | An explicit `this` receiver that the code resolves the same without. |
| `IfExpressionWithIdenticalBranches` | An `if` whose `then` and `else` branches are identical. |
| `RedundantGetter` | A getter that only returns the backing field. |
| `RedundantLabeledReturnOnLastExpressionInLambda` | A `return@label` on the last expression of the lambda with that label. |
| `RedundantLambdaOrAnonymousFunction` | A lambda or an anonymous function that is called at once. |
| `RedundantReturnLabel` | A label on a `return` that targets the enclosing function, not a lambda. |
| `RedundantSamConstructor` | A SAM constructor call in an argument where a plain lambda converts to the same type. |
| `RedundantSetter` | A setter that is the default one or only assigns the value to the backing field. |
| `RedundantUpperBound` | A type parameter bound that is `Any?`, the default upper bound. |
| `RedundantValueArgument` | An argument whose constant value equals the default value of the parameter. |
| `RedundantWith` | A `with` call whose lambda does not use the receiver. |
| `RemoveExplicitSuperQualifier` | A `super<T>` qualifier that does not change the member that the call resolves to. |
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
