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
python3 scripts/inspection-catalog.py <intellij-community checkout> --ref master
```

The script makes a sparse clone at the given path if it does not exist. Each rule names its
inspection with `@IntellijInspection`, and each rule set names its inspection group with
`@IntellijInspectionGroup`. The script reads these annotations.

Every week the [Upstream sync](.github/workflows/upstream-sync.yml) workflow runs
`scripts/inspection-sync.py`. The script compares the catalog with the `master` branch of
intellij-community and lists the new inspections, the removed inspections and the changed rules in
one issue with the `upstream-sync` label. Refresh the catalog to accept the changes.

## Rules

Every rule takes the standard detekt options (`active`, `excludes`, `includes`, ...). The rule
names are the short names of the IntelliJ inspections.

### `intellij-kotlin-code-migration`

| Rule | Finds |
|---|---|
| `CanConvertToMultiDollarString` | A string that escapes dollar characters and can use an interpolation prefix instead. Opposite of `ConvertFromMultiDollarToRegularString`; enable one of them. |
| `ConvertFromMultiDollarToRegularString` | A string with an interpolation prefix that can be a regular string. Opposite of `CanConvertToMultiDollarString`; enable one of them. |
| `ConvertLongToDuration` | A kotlinx.coroutines call that uses the `Long` milliseconds overload instead of `Duration`. |
| `InfixCallToOrdinary` | An infix call that can be an ordinary call. Opposite of `ConvertPairConstructorToToFunction` and `ReplaceToWithInfixForm`; enable one of them. |

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
| `EmptyRange` | A range whose constant start is past its end in the direction of the operator. |
| `FilterIsInstanceResultIsAlwaysEmpty` | A `filterIsInstance` call whose target type no element of the receiver can have. |
| `ForEachParameterNotUsed` | A `forEach` call whose lambda never uses the implicit `it` parameter. |
| `ImplicitNullableNothingType` | A `var` or open declaration without an explicit type whose inferred type is `Nothing?`. |
| `IncompleteDestructuring` | A destructuring declaration of a data class that omits some of its components. |
| `JavaIoSerializableObjectMustHaveReadResolve` | A `java.io.Serializable` object without `readResolve`. |
| `KotlinArrayHashCode` | A `hashCode()` call on an array that should be `contentHashCode()`. |
| `KotlinArrayToString` | An explicit or implicit `toString()` call on an array that should be `contentToString()`. |
| `KotlinEqualsBetweenInconvertibleTypes` | An `equals()` call between primitives, strings or enums of different types, which always returns false. |
| `KotlinMisorderedAssertEqualsArguments` | An `assertEquals()`-like call that passes the constant as the actual value and the tested value as the expected one. |
| `KotlinThrowableNotThrown` | A `Throwable` that a call creates or returns and that is never thrown, returned or used. |
| `LateinitVarOverridesLateinitVar` | A `lateinit var` that overrides a `lateinit var`. |
| `LazyWithoutDelegation` | A private or local property that holds a `Lazy` and reads it only through `value`, where `by` delegation fits. |
| `MainFunctionReturnUnit` | A `main` function that would be an entry point but does not return `Unit`. |
| `RecursiveEqualsCall` | An `equals` implementation that compares `this` with its parameter through `==`, `!=` or `equals`. |
| `RecursivePropertyAccessor` | A property accessor or a synthetic Java accessor override that accesses its own property. |
| `RedundantLabel` | A label on an expression that no `break`, `continue` or `return` can reference. |
| `SelfAssignment` | An assignment of a variable or property to itself. |
| `SelfReferenceConstructorParameter` | A primary constructor with a non-null parameter of its own class type, which no code can call. |
| `SetterBackingFieldAssignment` | A setter that never assigns the backing field, so the new value is lost. |
| `SuspiciousCallOnCollectionToAddOrRemovePath` | A `plus` or `minus` call that iterates over a self-iterable argument such as a `Path` instead of using it as one element. |
| `SuspiciousCascadingIf` | An operator or call after the last `else` block of a cascading `if` that applies only to the nested `if`. |
| `SuspiciousCollectionReassignment` | A `+=` or `-=` on a `var` of a read-only collection that creates a new collection each time. |
| `SuspiciousEqualsCombination` | A condition that compares the same variable with both `==` and `===`. |
| `SuspiciousGetterForMutableObject` | A getter that creates a new mutable collection or coroutine object on each access. |
| `SuspiciousJavaClassCallableReference` | A `::javaClass` callable reference that was meant to be `.javaClass` or `::class.java`. |
| `SuspiciousVarProperty` | A `var` property whose getter never reads the backing field, so the setter has no visible effect. |
| `UnusedDataClassCopyResult` | A data class `copy` call whose result is not used. |
| `UnusedEquals` | An `==` expression or `equals` call whose result is not used. |
| `UnusedExpression` | An expression whose value is not used and that has no effect. |
| `UnusedLambdaExpression` | A lambda expression that is a statement and is never called. |
| `UnusedLambdaExpressionBody` | An unused call of a function whose expression body is a lambda, so the lambda never runs. |
| `UselessCallOnCollection` | A `filterNotNull`, `filterIsInstance`, constant `filter` or `mapNotNull`-like call on a collection or sequence that does nothing or can be simpler. |
| `VariableNeverRead` | A local variable that gets values but is never read. |
| `WrapUnaryOperator` | A unary minus or plus before a number literal with a call, where the operator applies to the call result. |

### `intellij-kotlin-redundant-constructs`

| Rule | Finds |
|---|---|
| `CanBeParameter` | A `val` or `var` constructor parameter that is only used during initialization. Only parameters that are not visible outside the file are checked. |
| `CanBePrimaryConstructorProperty` | A property that is initialized from the constructor parameter of the same name and type. |
| `CanUnescapeDollarLiteral` | An escaped dollar in a string literal that can be a plain `$`. |
| `ConstantConditionIf` | An `if` whose condition is the constant `true` or `false`. |
| `ExplicitThis` | An explicit `this` receiver that the code resolves the same without. Opposite of `ImplicitThis`; enable one of them. |
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
| `SimplifyWhenWithBooleanConstantCondition` | A subjectless `when` with a `true` or `false` branch condition. |
| `SuspiciousCallableReferenceInLambda` | A lambda whose only statement is a callable reference. |
| `UnnecessaryOptInAnnotation` | An `@OptIn` annotation or marker that no used API requires. |
| `UnnecessaryVariable` | A local variable that only copies another local value or parameter. |
| `UnusedContextParameterCall` | A `context(...)` call whose block does not use all the arguments. |
| `UnusedReceiverParameter` | An extension receiver that is never used. |
| `VariableInitializerIsRedundant` | A variable initializer that is overwritten before it is read. |
| `WhenWithOnlyElse` | A `when` with only an `else` branch. |

### `intellij-kotlin-style-issues`

| Rule | Finds |
|---|---|
| `AddOperatorModifier` | A function that matches an operator convention but has no `operator` modifier. |
| `AddVarianceModifier` | A class type parameter that is used only in input or only in output positions and can be `in` or `out`. |
| `BooleanLiteralArgument` | Adjacent `true` or `false` arguments without parameter names. |
| `CascadeIf` | An `if`-`else if` chain on one subject that `when` can replace. |
| `CollectionConcatenationToBuildCollection` | A chain of two or more `+` or `-` operations on a list or set that `buildList` or `buildSet` can replace. |
| `ConvertPairConstructorToToFunction` | An explicit `Pair(a, b)` constructor call that can be the infix `a to b`. Opposite of `InfixCallToOrdinary`; enable one of them. |
| `ConvertRangeCheckToTwoComparisons` | An `in` or `!in` check of simple values against a standard range that can be two comparisons. Opposite of `ConvertTwoComparisonsToRangeCheck`; enable one of them. |
| `ConvertReferenceToLambda` | A callable reference that a lambda can replace. |
| `ConvertSealedClassToSealedInterface` | A sealed class without state, constructor parameters or final members that can be a sealed interface. Opposite of `ConvertSealedInterfaceToSealedClass`; enable one of them. |
| `ConvertSealedInterfaceToSealedClass` | A sealed interface whose inheritors are all plain classes or objects that can extend a sealed class. Opposite of `ConvertSealedClassToSealedInterface`; enable one of them. |
| `ConvertSecondaryConstructorToPrimary` | A secondary constructor that every other constructor delegates to and that can be the primary constructor. |
| `ConvertToExplicitBackingFields` | A property whose getter only returns a private property of a narrower type, which an explicit backing field can replace. Opposite of `RevertExplicitBackingFields`; enable one of them. |
| `ConvertToStringTemplate` | A `String` concatenation of literals and simple values that a string template can replace. |
| `ConvertTryFinallyToUseCall` | A `try`-`finally` that only closes a `Closeable` in `finally`, which `use()` can replace. |
| `ConvertTwoComparisonsToRangeCheck` | Two comparisons of one value with a lower and an upper bound that an `in` or `!in` range check can replace. Opposite of `ConvertRangeCheckToTwoComparisons`; enable one of them. |
| `CopyWithoutNamedArguments` | A data class `copy` call that passes arguments without names. |
| `DestructuringDeclaration` | A local variable, loop variable or lambda parameter of a data class or `Map.Entry` type that is only used to read its components. |
| `FilterIsInstanceCallWithClassLiteralArgument` | A `filterIsInstance(X::class.java)` call that `filterIsInstance<X>()` can replace. |
| `FoldInitializerAndIfToElvis` | An `if` null or type check that exits right after a variable declaration and can fold into the initializer with `?:`. |
| `IfThenToElvis` | An `if` that checks a value for `null` or a type and that the elvis operator `?:` can replace. |
| `IfThenToSafeAccess` | An `if` that checks a value for `null` or a type, returns `null` otherwise, and that `?.` or `as?` can replace. |
| `ImplicitThis` | A member access or callable reference that uses an implicit `this` receiver. Opposite of `ExplicitThis`; enable one of them. |
| `IntroduceWhenSubject` | A `when` without a subject whose branches all check the same value, which can be the subject. |
| `JavaMapForEach` | A call of the Java `Map.forEach` with a two-parameter lambda instead of the Kotlin `forEach` with a destructured entry. |
| `JoinDeclarationAndAssignment` | A property without an initializer that can join its first assignment. |
| `LiftReturnOrAssignment` | An `if`, `when` or `try` whose branches all end in a return or in an assignment to the same variable. |
| `MapToForEach` | A `map`-like call whose result is not used and that `forEach` or `forEachIndexed` can replace. |
| `MoveLambdaOutsideParentheses` | A lambda passed as the last argument inside the parentheses that can be a trailing lambda. |
| `MoveVariableDeclarationIntoWhen` | A variable declared right before a `when` and used only in it, which can move into the `when` subject. |
| `NullableHashCode` | An `x?.hashCode() ?: 0` on a nullable receiver that `x.hashCode()` can replace. |
| `RedundantAsSequence` | An `asSequence()` call on a sequence, or on an iterable followed by one terminal operation. |
| `RedundantElseInIf` | An `else` branch after `if` branches that all end with a jump such as `return` or `throw`. |
| `RedundantObjectTypeCheck` | An `is` check against a non-data object type that should be `===` or `!==`. |
| `RedundantRunCatching` | A `runCatching { }.getOrThrow()` chain that `run { }` can replace. |
| `RemoveEmptyParenthesesFromAnnotationEntry` | Empty parentheses after an annotation that needs no arguments. |
| `ReplaceAddAllWithMapTo` | An `addAll` or `+=` of a `map` or `filter` result that `mapTo` or `filterTo` can replace. |
| `ReplaceAssertBooleanWithAssertEquality` | An `assertTrue` or `assertFalse` call with an `==` or `===` check that an equality assertion can replace. |
| `ReplaceAssociateFunction` | An `associate` or `associateTo` call that `associateBy` or `associateWith` can replace. |
| `ReplaceCallWithBinaryOperator` | An explicit call of `equals`, `compareTo` or an arithmetic or range operator function that a binary operator can replace. |
| `ReplaceCollectionCountWithSize` | A `count()` call without a predicate on a collection, array or map that `size` can replace. |
| `ReplaceContains` | A call of an operator `contains` function that the `in` operator can replace. |
| `ReplaceIfExpressionWithFirstOrNull` | An `if` that returns the first element of a non-empty collection, string or array, else `null`, which `firstOrNull()` can replace. |
| `ReplaceJavaStaticMethodWithKotlinAnalog` | A call of a Java static method that has a Kotlin standard library counterpart. |
| `ReplaceManualRangeWithIndicesCalls` | A manual range from `0` to the size or last index that `indices`, a loop over the elements or `withIndex()` can replace. |
| `ReplaceMapGetOrDefault` | A `map.getOrDefault(key, default)` call on a map with non-null values that `map[key] ?: default` can replace. |
| `ReplaceMapIndexedWithListGenerator` | A `mapIndexed` call on a collection that ignores the element, where `List(size) { index -> ... }` fits. |
| `ReplaceMapKeysCallChainWithKeys` | A `map { it.key }.toSet()` call chain on a map that `keys` can replace. |
| `ReplaceNegatedIsEmptyWithIsNotEmpty` | A negated `isEmpty`, `isNotEmpty`, `isBlank` or `isNotBlank` call that the inverted function can replace. |
| `ReplaceNotNullAssertionWithElvisReturn` | A `!!` in a function or labeled lambda that returns `Unit` or a nullable type, where `?: return` fits. |
| `ReplaceRangeStartEndInclusiveWithFirstLast` | A `start` or `endInclusive` on a primitive range that the unboxed `first` or `last` can replace. |
| `ReplaceReadLineWithReadln` | A `readLine()` call that `readln()` or `readlnOrNull()` can replace. |
| `ReplaceSizeCheckWithIsNotEmpty` | A size, length or count comparison that `isNotEmpty()` can replace. |
| `ReplaceSizeZeroCheckWithIsEmpty` | A size, length or count zero check that `isEmpty()` can replace. |
| `ReplaceStringFormatWithLiteral` | A `String.format` call with only `%s` placeholders that a string template can replace. |
| `ReplaceSubstringWithDropLast` | An `s.substring(0, s.length - n)` call that `s.dropLast(n)` can replace. |
| `ReplaceSubstringWithIndexingOperation` | A `substring(i, i + 1)` call with constant indices that `s[i]` can replace. |
| `ReplaceSubstringWithSubstringAfter` | An `s.substring(s.indexOf(x))` call that `s.substringAfter(x)` can replace. |
| `ReplaceSubstringWithSubstringBefore` | An `s.substring(0, s.indexOf(x))` call that `s.substringBefore(x)` can replace. |
| `ReplaceSubstringWithTake` | An `s.substring(0, n)` call that `s.take(n)` can replace. |
| `ReplaceToStringWithStringTemplate` | A `toString()` call on a reference that a string template can replace. |
| `ReplaceToWithInfixForm` | A dot call of the infix `to` function that can use the infix form. Opposite of `InfixCallToOrdinary`; enable one of them. |
| `ReplaceUntilWithRangeUntil` | An infix `until` call from the standard library that the `..<` operator can replace. |
| `ReplaceWithCallWithContextCall` | A `with` call whose receiver is only used as a context argument, so a `context` call fits. |
| `ReplaceWithIgnoreCaseEquals` | An equality check of two identical case conversions that `equals(..., ignoreCase = true)` can replace. |
| `ReplaceWithImportAlias` | A qualified name that an existing import alias can replace. |
| `ReplaceWithOperatorAssignment` | An assignment such as `x = x + y` that an operator assignment such as `x += y` can replace. |
| `RevertExplicitBackingFields` | A property with an explicit backing field that a private backing property can replace. Opposite of `ConvertToExplicitBackingFields`; enable one of them. |
| `SafeCastWithReturn` | An `x as? T ?: return` statement that an `if (x !is T) return` check can replace. |
| `SimplifiableCall` | A `flatMap`, `filter` or `mapNotNull` call with a trivial lambda that `flatten`, `filterNotNull` or `filterIsInstance` can replace. |
| `SimplifiableCallChain` | A collection, sequence or text call chain that one call can replace. |
| `SimplifyAssertNotNull` | An `assert(x != null)` right after the declaration of `x` that can fold into the initializer. |
| `SimplifyBooleanWithConstants` | A boolean expression with a constant operand that can be simpler, such as `x && true`. |
| `SimplifyNegatedBinaryExpression` | A negated comparison, `is` or `in` check, or boolean literal that the inverted operator can replace. |
| `SimplifyNestedEachInScopeFunction` | An `also` or `apply` whose only statement is `forEach` or `onEach` on the receiver, which `onEach` can replace. |
| `UnclearPrecedenceOfBinaryExpression` | An expression that mixes `?:` with equality, comparison, `in`, `is`, arithmetic or infix calls, or `||` in a `when` guard, without parentheses. |
| `UnlabeledReturnInsideLambda` | An unlabeled `return` inside an inline lambda that returns from the enclosing function. |
| `UnsafeCastWithReturn` | An unsafe cast followed by `?: return` that should be the safe cast `as?`. |
| `UseExpressionBody` | A function or property accessor whose block body holds only a `return` or one `Unit` or `Nothing` expression. |
| `UsePropertyAccessSyntax` | A call of a Java getter or setter that Kotlin property access syntax can replace. |
| `VerboseNullabilityAndEmptiness` | A null check followed by an emptiness or blankness check that `isNullOrEmpty()` or `isNullOrBlank()` can replace. |

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
