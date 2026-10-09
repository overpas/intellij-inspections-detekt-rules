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

### `intellij-kotlin-code-migration`

| Rule | Finds |
|---|---|
| `CanConvertToMultiDollarString` | A string that escapes dollar characters and can use an interpolation prefix instead. |
| `ConvertFromMultiDollarToRegularString` | A string with an interpolation prefix that can be a regular string. |
| `ConvertLongToDuration` | A kotlinx.coroutines call that uses the `Long` milliseconds overload instead of `Duration`. |
| `InfixCallToOrdinary` | An infix call that can be an ordinary call. |

### `intellij-kotlin-coroutines`

| Rule | Finds |
|---|---|
| `CoroutineContextWithJob` | A `Job` or `NonCancellable` in the context argument of a coroutine builder, which breaks structured concurrency. |
| `DeferredResultUnused` | A call that returns a `Deferred` whose result is never used. |
| `ForEachJoinOnCollectionOfJob` | A `forEach { it.join() }` on a collection of jobs that `joinAll()` can replace. |
| `MapAwaitOnCollectionOfDeferred` | A `map { it.await() }` on a collection of deferred values that `awaitAll()` can replace. |
| `PreferCurrentCoroutineContextToCoroutineContext` | A use of `kotlin.coroutines.coroutineContext` that `currentCoroutineContext()` should replace. |
| `RunBlockingInSuspendFunction` | A `runBlocking` call inside a suspend function or a suspend lambda. |
| `SimplifiableFlowCall` | A `Flow` call with a trivial lambda that `flattenMerge`, `flattenConcat`, `filterNotNull` or `filterIsInstance` can replace. |
| `SimplifiableFlowCallChain` | A chain of two `Flow` calls that one call can replace, such as `filter {}.first()`. |
| `SuspendCoroutineLacksCancellationGuarantees` | A `suspendCoroutine` call that ignores cancellation when `suspendCancellableCoroutine` is available. |
| `SuspiciousImplicitCoroutineScopeReceiverAccess` | An implicit access to an outer `CoroutineScope` receiver from inside a suspending lambda or function. |
| `SuspiciousMutableCollectionInStateFlow` | A `MutableStateFlow` that holds a mutable collection, which emits no new value when it changes in place. |
| `UnusedFlow` | A `Flow` that is created but never collected, returned or passed on. |
| `UselessCallOnFlow` | A `filterNotNull`, `filterIsInstance` or `mapNotNull` call on a `Flow` that does nothing or can be simpler. |

### `intellij-kotlin-java-interop`

| Rule | Finds |
|---|---|
| `JavaCollectionWithNullableTypeArgument` | A Java concurrent collection or `PriorityQueue` with a nullable type argument, which does not support `null`. |
| `JavaCollectionsStaticMethodOnImmutableList` | A `java.util.Collections` mutator call (`reverse`, `sort`, `shuffle`, `fill`) on a read-only Kotlin list. |
| `JavaDefaultMethodsNotOverriddenByDelegation` | Interface delegation that does not forward the overrides of Java default methods of the delegate. |

### `intellij-kotlin-logging`

| Rule | Finds |
|---|---|
| `KotlinLoggerInitializedWithForeignClass` | A logger that is created with the class literal of another class. |

### `intellij-kotlin-migration`

| Rule | Finds |
|---|---|
| `KotlinDeprecation` | A deprecated symbol with a replacement, an import of such a symbol, deprecated syntax, or a useless cast, elvis, safe call or `!!`. |

### `intellij-kotlin-naming-conventions`

| Rule | Finds |
|---|---|
| `InconsistentCommentForJavaParameter` | A `/* name = */` comment before an argument of a Java call that does not match the parameter name. |
| `LocalVariableName` | A local variable, destructuring entry or non-property parameter whose name is not lowerCamelCase with only letters and digits. |

### `intellij-kotlin-numeric-issues`

| Rule | Finds |
|---|---|
| `KotlinBigDecimalEquals` | An `equals()` or `==` comparison of `BigDecimal` values that should use `compareTo()`. |

### `intellij-kotlin-other-problems`

| Rule | Finds |
|---|---|
| `ConvertArgumentToSet` | A collection argument of `minus`, `intersect`, `subtract`, `removeAll` or `retainAll` that should be a `Set`. |
| `DeprecatedCallableAddReplaceWith` | A `@Deprecated` callable with a simple body but no `replaceWith` argument. |
| `EnumValuesSoftDeprecate` | A `values()` call of an enum class that `entries` can replace. |
| `EnumValuesTopLevelFunctionSoftDeprecate` | An `enumValues<T>()` call that `enumEntries<T>()` can replace. |
| `FloatingPointLiteralPrecision` | A floating-point literal with more digits than its type can hold. |
| `MigrateDiagnosticSuppression` | An old diagnostic name in `@Suppress` that has a new name. |
| `ReplaceWithEnumMap` | A `HashMap` with enum keys that an `EnumMap` can replace. |
| `ReplaceWithStringBuilderAppendRange` | An `append(CharArray, Int, Int)` call that `appendRange` can replace. |

### `intellij-kotlin-probable-bugs`

| Rule | Finds |
|---|---|
| `AmbiguousNonLocalJump` | An unlabeled `break` or `continue` in an inline lambda inside a loop. |
| `ArrayInDataClass` | An array property in a data or value class that does not override `equals()` and `hashCode()`. |
| `AssignedValueIsNeverRead` | An assignment to a local variable whose value is never read. |
| `CanSealedSubClassBeObject` | A subclass of a sealed class that has no state and no `equals()` and can be an object. |
| `ConflictingExtensionProperty` | An extension property that a synthetic Java property of the receiver shadows. |
| `ConvertNaNEquality` | An equality check with `Double.NaN` or `Float.NaN` that should use `isNaN()`. |
| `DataClassPrivateConstructor` | A private primary constructor of a data class that the generated `copy()` exposes. |
| `DelegationToVarProperty` | Class delegation to a `var` constructor property that the class never reassigns. |
| `DuplicateArgumentsInSetOfAndMapOfFunctions` | A duplicate constant element in a `setOf` call or a duplicate constant key in a `mapOf` call. |

### `intellij-kotlin-redundant-constructs`

| Rule | Finds |
|---|---|
| `CanBeParameter` | A `val` or `var` constructor parameter that is only used during initialization. Only parameters that are not visible outside the file are checked. |
| `CanBePrimaryConstructorProperty` | A property that is initialized from the constructor parameter of the same name and type. |
| `CanUnescapeDollarLiteral` | An escaped dollar in a string literal that can be a plain `$`. |
| `ConstantConditionIf` | An `if` whose condition is the constant `true` or `false`. |
| `ExplicitThis` | An explicit `this` receiver that the code resolves the same without. |
| `IfExpressionWithIdenticalBranches` | An `if` whose `then` and `else` branches are identical. |
| `KotlinRedundantOverride` | An override that only calls the super implementation with the same arguments. |
| `NullChecksToSafeCall` | Chained null checks on a receiver and on a call on it that one safe call check can replace. |
| `RedundantCompanionReference` | An explicit companion object reference that the code resolves the same without. |
| `RedundantElvisReturnNull` | A `?: return null` in a returned expression that is null anyway. |
| `RedundantEnumConstructorInvocation` | Empty parentheses after an enum entry. |
| `RedundantGetter` | A getter that only returns the backing field. |
| `RedundantIf` | An `if` that only chooses between `true` and `false`, or a constant and a boolean expression. |
| `RedundantInterpolationPrefix` | A multi-dollar interpolation prefix on a string that does not need it. |
| `RedundantLabeledReturnOnLastExpressionInLambda` | A `return@label` on the last expression of the lambda with that label. |
| `RedundantLambdaArrow` | A lambda arrow with no parameters or with only an explicit `it` parameter. |
| `RedundantLambdaOrAnonymousFunction` | A lambda or an anonymous function that is called at once. |
| `RedundantModalityModifier` | A modality modifier that equals the default modality of the declaration. |
| `RedundantNullableReturnType` | A nullable return type of a function or a read-only property that never returns null. |
| `RedundantReturnKeyword` | A `return` in a branch of an `if`, `when` or elvis expression that is already returned. |
| `RedundantReturnLabel` | A label on a `return` that targets the enclosing function, not a lambda. |
| `RedundantSamConstructor` | A SAM constructor call in an argument where a plain lambda converts to the same type. |
| `RedundantSetter` | A setter that is the default one or only assigns the value to the backing field. |
| `RedundantUpperBound` | A type parameter bound that is `Any?`, the default upper bound. |
| `RedundantValueArgument` | An argument whose constant value equals the default value of the parameter. |
| `RedundantWith` | A `with` call whose lambda does not use the receiver. |
| `RemoveExplicitSuperQualifier` | A `super<T>` qualifier that does not change the member that the call resolves to. |
| `RemoveExplicitTypeArguments` | Explicit type arguments that the compiler can infer. |
| `RemoveRedundantQualifierName` | A package, class or companion qualifier that the code resolves the same without. |
| `RemoveRedundantSpreadOperator` | A spread operator on an array that is created in place with an `arrayOf`-like call or a `[...]` literal. |
| `RemoveSetterParameterType` | An explicit type on a setter parameter. |
| `RemoveSingleExpressionStringTemplate` | A string template that only holds one non-null `String` expression. |
| `ScopeFunctionConversion` | A scope function call that another scope function can replace. |
| `SimplifyWhenWithBooleanConstantCondition` | A subjectless `when` with a `true` or `false` branch condition. |
| `SuspiciousCallableReferenceInLambda` | A lambda whose only statement is a callable reference. |
| `UnnecessaryOptInAnnotation` | An `@OptIn` annotation or marker that no used API requires. |
| `UnnecessaryVariable` | A local variable that only copies another local value or parameter. |
| `UnusedContextParameterCall` | A `context(...)` call whose block does not use all the arguments. |
| `UnusedReceiverParameter` | An extension receiver that is never used. |
| `VariableInitializerIsRedundant` | A variable initializer that is overwritten before it is read. |
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
