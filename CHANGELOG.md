# Changelog

## 0.0.5

### Changed options and defaults

- `intellij-android-kotlin`: `IllegalIdentifier`: the default `excludes` now also match screenshot test source sets with `**/src/screenshotTest*/**`. These source sets run on the host JVM and are not compiled to dex. To keep the old behavior, set `excludes` to `['**/test/**', '**/src/test*/**', '**/commonTest/**', '**/jvmTest/**', '**/androidUnitTest/**', '**/androidHostTest/**']`.

### Changed detection

- `intellij-jvm-test-frameworks`: `AssertBetweenInconvertibleTypes`: treats a mutable collection type and its read-only type as one class, as IntelliJ IDEA does. It does not report an AssertJ `isEqualTo` between an `Iterable` and a class that implements `Iterable` by delegation. The old findings were false positives, so no option keeps them.
- `intellij-kotlin-redundant-constructs`: `UnnecessaryVariable`: does not report a copy of a local variable when a variable with the same name is visible in the scope, such as an inherited member or a top-level property. Removing the copy would change the meaning of the code, so no option keeps the old findings.
- `intellij-kotlin-style-issues`: `FoldInitializerAndIfToElvis`: reports only a `null` check. It does not report a negated type check, such as `if (x !is T) return`, because IntelliJ IDEA does not highlight it. No option keeps the old findings.
- `intellij-kotlin-style-issues`: `ImplicitThis`: does not report a companion object member that is used outside the companion object, because no `this@Label` form can name that receiver. The old findings were false positives, so no option keeps them.
- `intellij-kotlin-style-issues`: `ReplaceAssociateFunction`: does not report an `associate` call whose lambda computes both the key and the value, because IntelliJ IDEA does not highlight it. No option keeps the old findings.

### Changed messages

- `intellij-kotlin-style-issues`: `FoldInitializerAndIfToElvis`: the description no longer mentions type checks.

## 0.0.4

### Removed rules

- `intellij-kotlin-migration`: `KotlinDeprecation`: removed with its rule set, because the Kotlin compiler and detekt already report the same problems. To keep these findings, use the compiler warnings and the detekt rules `Deprecation`, `UnnecessarySafeCall`, `UnnecessaryNotNullOperator` and `UnreachableCode`. Remove the `intellij-kotlin-migration` section from your config.

### Changed options and defaults

- `intellij-android-kotlin`: `IllegalIdentifier`: the default `excludes` now match all unit test source sets with `**/src/test*/**`, for example `src/testStaging`. Before, only `test`, `testDebug` and `testRelease` were excluded. To keep the old behavior, set `excludes` to `['**/test/**', '**/testDebug/**', '**/testRelease/**', '**/commonTest/**', '**/jvmTest/**', '**/androidUnitTest/**', '**/androidHostTest/**']`.
- `intellij-kotlin-style-issues`: `DestructuringDeclaration`: new option `maxDestructuringEntries` (default `3`, as in detekt's `DestructuringDeclarationWithTooManyEntries`) does not report a variable when the suggested declaration has more entries. To keep the old behavior, set it to a large value, such as `100`.

### Changed detection

- `intellij-android-kotlin`: `IllegalIdentifier`: does not report identifiers in KDoc. KDoc is not compiled to Dalvik bytecode, so no option keeps the old findings.
- `intellij-kotlin-coroutines`: `UnusedFlow`: does not report a `Flow` call inside a MockK verification block, such as `verify {}`, `coVerify {}`, `verifyOrder {}` or `excludeRecords {}`. These calls only record the expected call. No option keeps the old findings.
- `intellij-kotlin-probable-bugs`: `CanSealedSubClassBeObject`: does not report a subclass when a superclass in its chain is a library class other than `Any`, such as `Exception`. The rule cannot prove that such a class has no state, so no option keeps the old findings.
- `intellij-kotlin-probable-bugs`: `KotlinMisorderedAssertEqualsArguments`: a `to*` or `from*` conversion counts as an expected value only on a literal or constant receiver, and an object receiver no longer counts as a factory qualifier. The empty and mutable collection factories, such as `emptyList()` and `mutableListOf()`, now count as expected values. The old findings were false positives, so no option keeps them.
- `intellij-kotlin-probable-bugs`: `SuspiciousCallOnCollectionToAddOrRemovePath`: does not report ranges and progressions. The rule reports an element-type argument only when that type is iterable over itself, as `java.nio.file.Path` is. No option keeps the old findings.
- `intellij-kotlin-redundant-constructs`: `ExplicitThis`: reports `this.` only when the name without it resolves to the same symbol with the same receivers. Before, the rule reported a `this.` that a Java getter property or another declaration shadows, and the code without `this.` had a different meaning. No option keeps the old findings.
- `intellij-kotlin-redundant-constructs`: `RedundantModalityModifier`: does not report `abstract` in an interface, because detekt's `OptionalAbstractKeyword` reports it. To keep these findings, activate `OptionalAbstractKeyword`.
- `intellij-kotlin-redundant-constructs`: `RemoveRedundantQualifierName`: does not check qualifiers in KDoc. Before, a KDoc link could stop the detekt task with a `NoSuchMethodError`. No option keeps the old behavior.
- `intellij-kotlin-style-issues`: `AddVarianceModifier`: takes the declared variance of nested type parameters, type projections and supertype type arguments into account. It does not suggest a variance that does not compile. No option keeps the old findings.
- `intellij-kotlin-style-issues`: `ConvertSealedClassToSealedInterface`: does not report a sealed class with `internal` or `protected` functions or properties, because an interface cannot have them. No option keeps the old findings.
- `intellij-kotlin-style-issues`: `ReplaceCallWithBinaryOperator`: reports only `equals` and `compareTo` calls, as IntelliJ IDEA highlights them. It does not report calls of arithmetic and range operator functions, `equals` with a floating-point operand, or `equals` on a receiver of a Java type with unknown nullability. No option keeps the old findings.

### Changed messages

- `intellij-kotlin-style-issues`: `ReplaceCallWithBinaryOperator`: the description now names only `equals` and `compareTo`.

### Distribution

- The `intellij-inspections-kotlin-migration-<version>.jar` is no longer attached to the release, and the bundle jars no longer contain the `intellij-kotlin-migration` rule set. Remove the dependency on this jar from your build.

## 0.0.3

### Changed options and defaults

- `intellij-kotlin-style-issues`: `ReplaceAssociateFunction`: inactive by default. To keep the old behavior, set `active: true`.

### Changed detection

- `intellij-jvm-general`: `BlockingMethodInNonBlockingContext`: a coroutine context argument that the rule cannot resolve to a known dispatcher, such as an injected `CoroutineDispatcher` property or parameter in `withContext(ioDispatcher)`, is now an unknown context. With the default `isUnknownContextBlocking: true`, blocking calls in it are not reported. Before, the rule reported them. To keep the old behavior, set `isUnknownContextBlocking` to `false`. This also reports blocking calls in all other unknown contexts.
- `intellij-kotlin-style-issues`: `UsePropertyAccessSyntax`: does not report a call of a Java setter when the synthetic property is read-only, for example when the setter parameter type is different from the getter type. A property assignment does not compile in this case, so no option keeps the old findings.

## 0.0.2

### Changed options and defaults

- `intellij-kotlin-style-issues`: `AddOperatorModifier`: new option `areSuspendFunctionsSkipped` (default `true`) does not report `suspend` functions. To keep the old behavior, set it to `false`.
- `intellij-kotlin-style-issues`: `ConvertReferenceToLambda`: inactive by default, because IntelliJ IDEA reports it only at INFORMATION level. To keep the old behavior, set `active: true`.
- `intellij-kotlin-style-issues`: `DestructuringDeclaration`: reports only loop variables by default. New option `areNonParameterCasesReported` (default `false`) also reports local variables and lambda parameters. To keep the old behavior, set it to `true`.
- `intellij-kotlin-style-issues`: `ReplaceToStringWithStringTemplate`: inactive by default, because IntelliJ IDEA reports it only at INFORMATION level. To keep the old behavior, set `active: true`.
- `intellij-kotlin-style-issues`: `UseExpressionBody`: new option `areUnitExpressionsSkipped` (default `true`) does not report empty block bodies and block bodies with one `Unit` expression. To keep the old behavior, set it to `false`.

### Changed detection

- `intellij-kotlin-redundant-constructs`: `UnnecessaryOptInAnnotation`: now reports an unnecessary `@OptIn` on a function that has parameters of a function type, such as `(value: Int) -> String`. Before, the parameters of the function type hid the finding. To keep the old behavior, suppress the new findings.

### Distribution

- The README setup now tells to run the detekt tasks with type resolution, such as `detektMain` and `detektTest`. Most rules need type resolution, and the plain `detekt` task skips them without a report. In a Kotlin Multiplatform project, run the task of each JVM target, for example `detektMainJvm`.

## 0.0.1

### Added rules

- `intellij-android-compose-preview`: `ComposePreviewDimensionRespectsLimit`: finds a `@Preview` whose `widthDp` or `heightDp` is not between 1 and 3000.
- `intellij-android-compose-preview`: `ComposePreviewMustBeTopLevelFunction`: finds a function with `@Preview` or a MultiPreview annotation that is not top level and is not a non-nested function of a top-level class with a no-parameter constructor.
- `intellij-android-compose-preview`: `ComposePreviewNeedsComposableAnnotation`: finds a function with `@Preview` or a MultiPreview annotation that is not `@Composable`.
- `intellij-android-compose-preview`: `PreviewAnnotationInFunctionWithParameters`: finds a function with `@Preview` or a MultiPreview annotation that has a parameter with no default value and no `@PreviewParameter`.
- `intellij-android-compose-preview`: `PreviewApiLevelMustBeValid`: finds a `@Preview` whose `apiLevel` is outside the supported range (`minApiLevel`, default 22, to `maxApiLevel`, default 37).
- `intellij-android-compose-preview`: `PreviewDeviceShouldUseNewSpec`: finds a `@Preview` whose `device` is a legacy device spec that starts with `spec:id`, such as the old `Devices.PIXEL_4`.
- `intellij-android-compose-preview`: `PreviewFontScaleMustBeGreaterThanZero`: finds a `@Preview` whose `fontScale` is zero or negative.
- `intellij-android-compose-preview`: `PreviewMultipleParameterProviders`: finds a function with `@Preview` or a MultiPreview annotation that has more than one `@PreviewParameter`.
- `intellij-android-compose-preview`: `PreviewParameterProviderOnFirstParameter`: finds a function with `@Preview` or a MultiPreview annotation that has its first `@PreviewParameter` on a parameter other than the first.
- `intellij-android-compose-preview`: `PreviewShouldNotBeCalledRecursively`: finds a call inside a function with `@Preview` or a MultiPreview annotation to a function with the same name as that preview. Inactive by default.
- `intellij-android-compose-preview`: `PreviewWrapperUsage`: finds a `@PreviewWrapper` on a function without both `@Composable` and `@Preview` (or a MultiPreview), a repeated `@PreviewWrapper` on an annotation class, or a composable function or annotation class with more than one `@PreviewWrapper` in its annotation graph.
- `intellij-android-glance-preview`: `GlancePreviewDimensionRespectsLimit`: finds a Glance `@Preview` annotation on a function or an annotation class with a constant `widthDp` or `heightDp` value outside 1..3000.
- `intellij-android-glance-preview`: `GlancePreviewMustBeTopLevelFunction`: finds a Glance `@Preview` or MultiPreview annotation on a function that is not top level and not in a top level class with a default constructor.
- `intellij-android-glance-preview`: `GlancePreviewNeedsComposableAnnotation`: finds a Glance `@Preview` or MultiPreview annotation on a function that is not `@Composable`.
- `intellij-android-kotlin`: `IllegalIdentifier`: finds a backticked identifier with characters that the dex format does not allow in a simple name, for example a space or a dot. These names fail on Android versions before 11. By default, unit test source sets are excluded (`excludes`).
- `intellij-android-wear-tile-preview`: `WearTilePreviewComposableAnnotationIsNotSupported`: finds a `@Composable` annotation on a Tile preview function.
- `intellij-android-wear-tile-preview`: `WearTilePreviewContextComesFromParameter`: finds a `Context` in a Tile preview function that does not come from the `Context` parameter of the function.
- `intellij-android-wear-tile-preview`: `WearTilePreviewHasValidMethodSignature`: finds a function with a Tile `@Preview` annotation that does not return `TilePreviewData`, or has parameters other than one `Context`.
- `intellij-android-wear-tile-preview`: `WearTilePreviewMethodIsAnnotatedWithTilePreviewAnnotation`: finds a `Preview` annotation from another package, or a MultiPreview annotation that refers to one, on a function with the Tile preview signature.
- `intellij-android-wear-tile-preview`: `WearTilePreviewShouldBeTopLevelFunction`: finds a function with a Tile `@Preview` annotation in another function, in a nested class, or in a class without a default constructor.
- `intellij-compose-multiplatform-preview`: `MultiplatformPreviewAnnotationInFunctionWithParameters`: finds a preview function that has a parameter with no default value and no `@PreviewParameter`.
- `intellij-compose-multiplatform-preview`: `MultiplatformPreviewMultipleParameterProviders`: finds a preview function that has more than one `@PreviewParameter`.
- `intellij-gradle-best-practices`: `AvoidApplyPluginMethod`: finds an `apply(plugin = ...)` or `apply<T>()` call in place of the `plugins` block.
- `intellij-gradle-best-practices`: `AvoidDependencyNamedArgumentsNotation`: finds a dependency in a `dependencies` block with a named `group` argument or with positional string coordinates, in place of one `group:name:version` string.
- `intellij-gradle-best-practices`: `AvoidDuplicateDependencies`: finds a dependency that the top-level `dependencies` blocks declare more than one time in the same configuration. It does not check the configurations that extend other configurations.
- `intellij-gradle-best-practices`: `AvoidDuplicateRepositories`: finds a repository that one `repositories` block declares more than one time. Whitespace and comments do not count.
- `intellij-gradle-best-practices`: `AvoidRepositoriesInBuildGradle`: finds a `repositories` block in a `build.gradle.kts` script, except in `publishing`. Inactive by default.
- `intellij-gradle-best-practices`: `RedundantKotlinStdLibDependency`: finds a `kotlin-stdlib` dependency with the version of the `kotlin("jvm")` plugin, which adds this dependency itself. It does not read `kotlin.stdlib.default.dependency` from `gradle.properties`.
- `intellij-gradle-best-practices`: `TaskMissingDescription`: finds a task that `tasks.register`, `tasks.create`, `tasks.registering` or `tasks.creating` makes without a `description`.
- `intellij-jvm-code-maturity`: `MarkedForRemoval`: finds a usage of an API that is deprecated with `@Deprecated(forRemoval = true)`, or an override of a method marked for removal. Option `isSameOutermostClassIgnored` (default `true`) does not report the usages in the same outermost class.
- `intellij-jvm-declaration-redundancy`: `SameParameterValue`: finds a parameter of a function that gets the same constant value in every call, when all calls are in the same file. Option `highestModifier` (default `protected`) is the highest visibility to check. Option `minimalUsageCount` (default `1`) is the number of calls needed to report.
- `intellij-jvm-declaration-redundancy`: `SameReturnValue`: finds a function that cannot be overridden and that always returns the same constant value.
- `intellij-jvm-general`: `BlockingMethodInNonBlockingContext`: finds a possibly blocking call, such as `Thread.sleep()`, a JDK method that throws `IOException` or a method annotated with `@Blocking`, in a non-blocking context such as a suspend function, a coroutine on a non-IO dispatcher or a method annotated with `@NonBlocking`. Option `isUnknownContextBlocking` (default `true`) does not report the calls in contexts that are not known. Option `isSuspendContextNonBlocking` (default `true`) considers suspend functions and suspend lambdas non-blocking. Options `blockingAnnotations` and `nonBlockingAnnotations` add annotations to the default ones.
- `intellij-jvm-general`: `MissingDeprecatedAnnotationOnScheduledForRemovalApi`: finds a declaration marked with `@ApiStatus.ScheduledForRemoval` that is not also marked with `@Deprecated`.
- `intellij-jvm-general`: `MustAlreadyBeRemovedApi`: finds a declaration marked with `@ApiStatus.ScheduledForRemoval` for a version that is not later than the current version. Option `currentVersion` (default empty, which turns the rule off) gives the current version.
- `intellij-jvm-general`: `NonExtendableApiUsage`: finds a class, interface or override that extends, implements or overrides a library API marked with `@ApiStatus.NonExtendable`, or the annotation on a declaration that cannot be extended.
- `intellij-jvm-general`: `OverrideOnly`: finds a call or reference of a library method marked with `@ApiStatus.OverrideOnly` outside a super or delegate call in its override, or the annotation on a declaration that cannot be overridden.
- `intellij-jvm-general`: `SerializableHasSerialVersionUIDField`: finds a `Serializable` class without a `serialVersionUID` field. Option `isAnonymousClassIgnored` (default `false`) does not report anonymous objects. Option `ignoredSuperClasses` (default `java.awt.Component`) does not report the subclasses of these classes that do not implement `Serializable` directly. Inactive by default.
- `intellij-jvm-general`: `SuppressionAnnotation`: finds a `@Suppress` or `@SuppressWarnings` annotation or a `//noinspection` comment that suppresses inspections. Option `allowedSuppressions` lists the suppressed ids to accept. Inactive by default.
- `intellij-jvm-general`: `SystemGetProperty`: finds a `System.getProperty()` call for `file.separator`, `path.separator`, `line.separator` or `file.encoding`, which a dedicated API gives directly.
- `intellij-jvm-general`: `ThreadRun`: finds a call of `run()` on a `Thread`, which runs the code in the current thread instead of starting a new one.
- `intellij-jvm-general`: `UnstableApiUsage`: finds a usage of a library API that an unstable API annotation such as `@ApiStatus.Experimental` or `@ApiStatus.Internal` marks, or whose signature refers to such a type. Option `unstableApiAnnotations` lists the annotations. Option `isInsideImportsIgnored` (default `true`) does not report imports. Option `isApiDeclaredInThisProjectIgnored` (default `true`) reports only compiled library APIs.
- `intellij-jvm-general`: `UnstableTypeUsedInSignature`: finds a public declaration whose signature refers to a type marked with an unstable API annotation, but that does not have the same annotation itself. Option `unstableApiAnnotations` lists the annotations. Inactive by default.
- `intellij-jvm-general`: `UrlHashCode`: finds an `equals()` or `hashCode()` call or an `==` comparison on `java.net.URL`, or a map or set of URLs, which can resolve the host over the network.
- `intellij-jvm-junit`: `AssertEqualsCalledOnArray`: finds an `assertEquals()` call on two arrays, which compares them by identity and not by content. Inactive by default.
- `intellij-jvm-junit`: `AssertEqualsMayBeAssertSame`: finds an `assertEquals()` call on two objects of a final class that does not override `equals()`, where `assertSame()` fits. Inactive by default.
- `intellij-jvm-junit`: `IgnoredJUnitTest`: finds a test class or test method that `@Ignore` or `@Disabled` turns off. Option `isReasonAccepted` (default `true`) does not report the ones that give a reason. Inactive by default.
- `intellij-jvm-junit`: `JUnit3StyleTestMethodInJUnit4Class`: finds a `test...` method without `@Test` in a JUnit 4 class, which JUnit 4 does not run.
- `intellij-jvm-junit`: `JUnit5AssertionsConverter`: finds a JUnit 3 or JUnit 4 assertion or assumption in a JUnit 5 test.
- `intellij-jvm-junit`: `JUnitMalformedDeclaration`: finds a JUnit test class, test method, lifecycle method, rule, extension or parameter source with a declaration that JUnit cannot run or rejects. Option `ignorableAnnotations` (default `mockit.Mocked`, `org.junit.jupiter.api.io.TempDir`) lists the parameter annotations to accept. Option `areSuspendFunctionsAllowed` (default `false`) accepts suspend test and lifecycle functions.
- `intellij-jvm-junit`: `JUnitMixedFramework`: finds a test class that uses annotations of a JUnit version other than the version of the class.
- `intellij-jvm-junit`: `Junit4RunWithInspection`: finds a `@RunWith` on a test class whose superclass already has `@RunWith`.
- `intellij-jvm-junit`: `MigrateAssertToMatcherAssert`: finds a JUnit assertion such as `assertEquals()` that a Hamcrest `assertThat()` can replace, when Hamcrest is on the classpath. Inactive by default.
- `intellij-jvm-junit`: `ParameterizedParametersStaticCollection`: finds a JUnit 4 `Parameterized` test class with no `@Parameters` method, more than one, or one with a bad signature. Inactive by default.
- `intellij-jvm-junit`: `SuperTearDownInFinally`: finds a `super.tearDown()` call in a JUnit 3 `tearDown()` that is not in a `finally` block. Inactive by default.
- `intellij-jvm-junit`: `UseOfObsoleteAssert`: finds a call of the JUnit 3 `junit.framework.Assert` or `TestCase` assertions, when JUnit 4 or later is on the classpath. Inactive by default.
- `intellij-jvm-logging`: `LogStatementNotGuardedByLogCondition`: finds a logging call with computed arguments that a log level condition such as `if (logger.isDebugEnabled)` does not guard. Option `limitLevelType` (default `DEBUG_AND_LOWER`) sets the log levels to check. Option `isUnguardedConstantFlagged` (default `false`) also reports the calls with only constant arguments. Inactive by default.
- `intellij-jvm-logging`: `LoggingConditionDisagreesWithLogLevelStatement`: finds a log level condition whose level does not agree with the level of the logging call that it guards. Inactive by default.
- `intellij-jvm-logging`: `LoggingPlaceholderCountMatchesArgumentCount`: finds a logging call whose number of placeholders does not match the number of arguments. Option `slf4jToLog4J2Type` (default `AUTO`) tells if SLF4J calls go to Log4j 2 (`AUTO`, `YES` or `NO`).
- `intellij-jvm-logging`: `LoggingStringTemplateAsArgument`: finds a logging call that builds its message with a string template or concatenation instead of placeholders. Option `limitLevelType` (default `DEBUG_AND_LOWER`) sets the log levels to check. Option `arePrimitivesSkipped` (default `true`) does not report templates with only primitive values. Option `isOnlyExceptionSkipped` (default `false`) does not report calls whose only other argument is an exception.
- `intellij-jvm-test-frameworks`: `AssertBetweenInconvertibleTypes`: finds a JUnit, TestNG or AssertJ equality or identity assertion between objects of inconvertible types.
- `intellij-jvm-test-frameworks`: `TestCaseWithConstructor`: finds a test class that does initialization work in a constructor or an `init` block instead of a set-up method. Inactive by default.
- `intellij-jvm-test-frameworks`: `TestCaseWithNoTestMethods`: finds a test class without test methods. Option `isSupersIgnored` (default `true`) does not report a class when a superclass has test methods. Inactive by default.
- `intellij-jvm-test-frameworks`: `TestMethodWithoutAssertion`: finds a test method without assertions. Option `assertionMethods` lists the assertion methods as `<class or package>#<method name regex>`. Option `isAssertKeywordAssertion` (default `true`) counts `assert()` as an assertion. Option `isThrowingIgnored` (default `false`) does not report a method with `@Throws`. Inactive by default.
- `intellij-kotlin-code-migration`: `CanConvertToMultiDollarString`: finds a string that escapes dollar characters and can use an interpolation prefix instead. Opposite of `ConvertFromMultiDollarToRegularString`.
- `intellij-kotlin-code-migration`: `ConvertFromMultiDollarToRegularString`: finds a string with an interpolation prefix that can be a regular string. Opposite of `CanConvertToMultiDollarString`, which is active by default. Inactive by default.
- `intellij-kotlin-code-migration`: `ConvertLongToDuration`: finds a kotlinx.coroutines call that uses the `Long` milliseconds overload instead of `Duration`.
- `intellij-kotlin-code-migration`: `InfixCallToOrdinary`: finds an infix call that can be an ordinary call. Opposite of `ConvertPairConstructorToToFunction` and `ReplaceToWithInfixForm`, which are active by default. Inactive by default.
- `intellij-kotlin-coroutines`: `CoroutineContextWithJob`: finds a `Job` or `NonCancellable` in the context argument of a coroutine builder, which breaks structured concurrency.
- `intellij-kotlin-coroutines`: `DeferredResultUnused`: finds a call that returns a `Deferred` whose result is never used.
- `intellij-kotlin-coroutines`: `ForEachJoinOnCollectionOfJob`: finds a `forEach { it.join() }` on a collection of jobs that `joinAll()` can replace.
- `intellij-kotlin-coroutines`: `MapAwaitOnCollectionOfDeferred`: finds a `map { it.await() }` on a collection of deferred values that `awaitAll()` can replace.
- `intellij-kotlin-coroutines`: `PreferCurrentCoroutineContextToCoroutineContext`: finds a use of `kotlin.coroutines.coroutineContext` that `currentCoroutineContext()` should replace.
- `intellij-kotlin-coroutines`: `RunBlockingInSuspendFunction`: finds a `runBlocking` call inside a suspend function or a suspend lambda.
- `intellij-kotlin-coroutines`: `SimplifiableFlowCall`: finds a `Flow` call with a trivial lambda that `flattenMerge`, `flattenConcat`, `filterNotNull` or `filterIsInstance` can replace.
- `intellij-kotlin-coroutines`: `SimplifiableFlowCallChain`: finds a chain of two `Flow` calls that one call can replace, such as `filter {}.first()`.
- `intellij-kotlin-coroutines`: `SuspendCoroutineLacksCancellationGuarantees`: finds a `suspendCoroutine` call that ignores cancellation when `suspendCancellableCoroutine` is available.
- `intellij-kotlin-coroutines`: `SuspiciousImplicitCoroutineScopeReceiverAccess`: finds an implicit access to an outer `CoroutineScope` receiver from inside a suspending lambda or function.
- `intellij-kotlin-coroutines`: `SuspiciousMutableCollectionInStateFlow`: finds a `MutableStateFlow` that holds a mutable collection, which emits no new value when it changes in place.
- `intellij-kotlin-coroutines`: `UnusedFlow`: finds a `Flow` that is created but never collected, returned or passed on.
- `intellij-kotlin-coroutines`: `UselessCallOnFlow`: finds a `filterNotNull`, `filterIsInstance` or `mapNotNull` call on a `Flow` that does nothing or can be simpler.
- `intellij-kotlin-java-interop`: `JavaCollectionWithNullableTypeArgument`: finds a Java concurrent collection or `PriorityQueue` with a nullable type argument, which does not support `null`.
- `intellij-kotlin-java-interop`: `JavaCollectionsStaticMethodOnImmutableList`: finds a `java.util.Collections` mutator call (`reverse`, `sort`, `shuffle`, `fill`) on a read-only Kotlin list.
- `intellij-kotlin-java-interop`: `JavaDefaultMethodsNotOverriddenByDelegation`: finds interface delegation that does not forward the overrides of Java default methods of the delegate.
- `intellij-kotlin-logging`: `KotlinLoggerInitializedWithForeignClass`: finds a logger that is created with the class literal of another class. Inactive by default.
- `intellij-kotlin-migration`: `KotlinDeprecation`: finds a deprecated symbol with a replacement, an import of such a symbol, deprecated syntax, or a useless cast, elvis, safe call or `!!`.
- `intellij-kotlin-naming-conventions`: `InconsistentCommentForJavaParameter`: finds a `/* name = */` comment before an argument of a Java call that does not match the parameter name.
- `intellij-kotlin-naming-conventions`: `LocalVariableName`: finds a local variable, destructuring entry or non-property parameter whose name is not lowerCamelCase with only letters and digits.
- `intellij-kotlin-numeric-issues`: `KotlinBigDecimalEquals`: finds an `equals()` or `==` comparison of `BigDecimal` values that should use `compareTo()`. Inactive by default.
- `intellij-kotlin-other-problems`: `ConvertArgumentToSet`: finds a collection argument of `minus`, `intersect`, `subtract`, `removeAll` or `retainAll` that should be a `Set`.
- `intellij-kotlin-other-problems`: `DeprecatedCallableAddReplaceWith`: finds a `@Deprecated` callable with a simple body but no `replaceWith` argument.
- `intellij-kotlin-other-problems`: `EnumValuesSoftDeprecate`: finds a `values()` call of an enum class that `entries` can replace.
- `intellij-kotlin-other-problems`: `EnumValuesTopLevelFunctionSoftDeprecate`: finds an `enumValues<T>()` call that `enumEntries<T>()` can replace.
- `intellij-kotlin-other-problems`: `FloatingPointLiteralPrecision`: finds a floating-point literal with more digits than its type can hold.
- `intellij-kotlin-other-problems`: `MigrateDiagnosticSuppression`: finds an old diagnostic name in `@Suppress` that has a new name.
- `intellij-kotlin-other-problems`: `ReplaceWithEnumMap`: finds a `HashMap` with enum keys that an `EnumMap` can replace.
- `intellij-kotlin-other-problems`: `ReplaceWithStringBuilderAppendRange`: finds an `append(CharArray, Int, Int)` call that `appendRange` can replace.
- `intellij-kotlin-probable-bugs`: `AmbiguousNonLocalJump`: finds an unlabeled `break` or `continue` in an inline lambda inside a loop.
- `intellij-kotlin-probable-bugs`: `ArrayInDataClass`: finds an array property in a data or value class that does not override `equals()` and `hashCode()`.
- `intellij-kotlin-probable-bugs`: `AssignedValueIsNeverRead`: finds an assignment to a local variable whose value is never read.
- `intellij-kotlin-probable-bugs`: `CanSealedSubClassBeObject`: finds a subclass of a sealed class that has no state and no `equals()` and can be an object.
- `intellij-kotlin-probable-bugs`: `ConflictingExtensionProperty`: finds an extension property that a synthetic Java property of the receiver shadows.
- `intellij-kotlin-probable-bugs`: `ConvertNaNEquality`: finds an equality check with `Double.NaN` or `Float.NaN` that should use `isNaN()`.
- `intellij-kotlin-probable-bugs`: `DataClassPrivateConstructor`: finds a private primary constructor of a data class that the generated `copy()` exposes.
- `intellij-kotlin-probable-bugs`: `DelegationToVarProperty`: finds class delegation to a `var` constructor property that the class never reassigns.
- `intellij-kotlin-probable-bugs`: `DuplicateArgumentsInSetOfAndMapOfFunctions`: finds a duplicate constant element in a `setOf` call or a duplicate constant key in a `mapOf` call.
- `intellij-kotlin-probable-bugs`: `EmptyRange`: finds a range whose constant start is past its end in the direction of the operator.
- `intellij-kotlin-probable-bugs`: `FilterIsInstanceResultIsAlwaysEmpty`: finds a `filterIsInstance` call whose target type no element of the receiver can have.
- `intellij-kotlin-probable-bugs`: `ForEachParameterNotUsed`: finds a `forEach` call whose lambda never uses the implicit `it` parameter.
- `intellij-kotlin-probable-bugs`: `ImplicitNullableNothingType`: finds a `var` or open declaration without an explicit type whose inferred type is `Nothing?`.
- `intellij-kotlin-probable-bugs`: `IncompleteDestructuring`: finds a destructuring declaration of a data class that omits some of its components.
- `intellij-kotlin-probable-bugs`: `JavaIoSerializableObjectMustHaveReadResolve`: finds a `java.io.Serializable` object without `readResolve`.
- `intellij-kotlin-probable-bugs`: `KotlinArrayHashCode`: finds a `hashCode()` call on an array that should be `contentHashCode()`.
- `intellij-kotlin-probable-bugs`: `KotlinArrayToString`: finds an explicit or implicit `toString()` call on an array that should be `contentToString()`.
- `intellij-kotlin-probable-bugs`: `KotlinEqualsBetweenInconvertibleTypes`: finds an `equals()` call between primitives, strings or enums of different types, which always returns false.
- `intellij-kotlin-probable-bugs`: `KotlinMisorderedAssertEqualsArguments`: finds an `assertEquals()`-like call that passes the constant as the actual value and the tested value as the expected one.
- `intellij-kotlin-probable-bugs`: `KotlinThrowableNotThrown`: finds a `Throwable` that a call creates or returns and that is never thrown, returned or used.
- `intellij-kotlin-probable-bugs`: `LateinitVarOverridesLateinitVar`: finds a `lateinit var` that overrides a `lateinit var`.
- `intellij-kotlin-probable-bugs`: `LazyWithoutDelegation`: finds a private or local property that holds a `Lazy` and reads it only through `value`, where `by` delegation fits.
- `intellij-kotlin-probable-bugs`: `MainFunctionReturnUnit`: finds a `main` function that would be an entry point but does not return `Unit`.
- `intellij-kotlin-probable-bugs`: `RecursiveEqualsCall`: finds an `equals` implementation that compares `this` with its parameter through `==`, `!=` or `equals`.
- `intellij-kotlin-probable-bugs`: `RecursivePropertyAccessor`: finds a property accessor or a synthetic Java accessor override that accesses its own property.
- `intellij-kotlin-probable-bugs`: `RedundantLabel`: finds a label on an expression that no `break`, `continue` or `return` can reference.
- `intellij-kotlin-probable-bugs`: `SelfAssignment`: finds an assignment of a variable or property to itself.
- `intellij-kotlin-probable-bugs`: `SelfReferenceConstructorParameter`: finds a primary constructor with a non-null parameter of its own class type, which no code can call.
- `intellij-kotlin-probable-bugs`: `SetterBackingFieldAssignment`: finds a setter that never assigns the backing field, so the new value is lost.
- `intellij-kotlin-probable-bugs`: `SuspiciousCallOnCollectionToAddOrRemovePath`: finds a `plus` or `minus` call that iterates over a self-iterable argument such as a `Path` instead of using it as one element.
- `intellij-kotlin-probable-bugs`: `SuspiciousCascadingIf`: finds an operator or call after the last `else` block of a cascading `if` that applies only to the nested `if`.
- `intellij-kotlin-probable-bugs`: `SuspiciousCollectionReassignment`: finds a `+=` or `-=` on a `var` of a read-only collection that creates a new collection each time.
- `intellij-kotlin-probable-bugs`: `SuspiciousEqualsCombination`: finds a condition that compares the same variable with both `==` and `===`.
- `intellij-kotlin-probable-bugs`: `SuspiciousGetterForMutableObject`: finds a getter that creates a new mutable collection or coroutine object on each access.
- `intellij-kotlin-probable-bugs`: `SuspiciousJavaClassCallableReference`: finds a `::javaClass` callable reference that was meant to be `.javaClass` or `::class.java`.
- `intellij-kotlin-probable-bugs`: `SuspiciousVarProperty`: finds a `var` property whose getter never reads the backing field, so the setter has no visible effect.
- `intellij-kotlin-probable-bugs`: `UnusedDataClassCopyResult`: finds a data class `copy` call whose result is not used.
- `intellij-kotlin-probable-bugs`: `UnusedEquals`: finds an `==` expression or `equals` call whose result is not used.
- `intellij-kotlin-probable-bugs`: `UnusedExpression`: finds an expression whose value is not used and that has no effect.
- `intellij-kotlin-probable-bugs`: `UnusedLambdaExpression`: finds a lambda expression that is a statement and is never called.
- `intellij-kotlin-probable-bugs`: `UnusedLambdaExpressionBody`: finds an unused call of a function whose expression body is a lambda, so the lambda never runs.
- `intellij-kotlin-probable-bugs`: `UselessCallOnCollection`: finds a `filterNotNull`, `filterIsInstance`, constant `filter` or `mapNotNull`-like call on a collection or sequence that does nothing or can be simpler.
- `intellij-kotlin-probable-bugs`: `VariableNeverRead`: finds a local variable that gets values but is never read.
- `intellij-kotlin-probable-bugs`: `WrapUnaryOperator`: finds a unary minus or plus before a number literal with a call, where the operator applies to the call result.
- `intellij-kotlin-redundant-constructs`: `CanBeParameter`: finds a `val` or `var` constructor parameter that is only used during initialization. Only parameters that are not visible outside the file are checked.
- `intellij-kotlin-redundant-constructs`: `CanBePrimaryConstructorProperty`: finds a property that is initialized from the constructor parameter of the same name and type.
- `intellij-kotlin-redundant-constructs`: `CanUnescapeDollarLiteral`: finds an escaped dollar in a string literal that can be a plain `$`.
- `intellij-kotlin-redundant-constructs`: `ConstantConditionIf`: finds an `if` whose condition is the constant `true` or `false`.
- `intellij-kotlin-redundant-constructs`: `ExplicitThis`: finds an explicit `this` receiver that the code resolves the same without. Opposite of `ImplicitThis`.
- `intellij-kotlin-redundant-constructs`: `IfExpressionWithIdenticalBranches`: finds an `if` whose `then` and `else` branches are identical.
- `intellij-kotlin-redundant-constructs`: `KotlinRedundantOverride`: finds an override that only calls the super implementation with the same arguments.
- `intellij-kotlin-redundant-constructs`: `NullChecksToSafeCall`: finds chained null checks on a receiver and on a call on it that one safe call check can replace.
- `intellij-kotlin-redundant-constructs`: `RedundantCompanionReference`: finds an explicit companion object reference that the code resolves the same without.
- `intellij-kotlin-redundant-constructs`: `RedundantElvisReturnNull`: finds a `?: return null` in a returned expression that is null anyway.
- `intellij-kotlin-redundant-constructs`: `RedundantEnumConstructorInvocation`: finds empty parentheses after an enum entry.
- `intellij-kotlin-redundant-constructs`: `RedundantGetter`: finds a getter that only returns the backing field.
- `intellij-kotlin-redundant-constructs`: `RedundantIf`: finds an `if` that only chooses between `true` and `false`, or a constant and a boolean expression.
- `intellij-kotlin-redundant-constructs`: `RedundantInterpolationPrefix`: finds a multi-dollar interpolation prefix on a string that does not need it.
- `intellij-kotlin-redundant-constructs`: `RedundantLabeledReturnOnLastExpressionInLambda`: finds a `return@label` on the last expression of the lambda with that label.
- `intellij-kotlin-redundant-constructs`: `RedundantLambdaArrow`: finds a lambda arrow with no parameters or with only an explicit `it` parameter.
- `intellij-kotlin-redundant-constructs`: `RedundantLambdaOrAnonymousFunction`: finds a lambda or an anonymous function that is called at once.
- `intellij-kotlin-redundant-constructs`: `RedundantModalityModifier`: finds a modality modifier that equals the default modality of the declaration.
- `intellij-kotlin-redundant-constructs`: `RedundantNullableReturnType`: finds a nullable return type of a function or a read-only property that never returns null.
- `intellij-kotlin-redundant-constructs`: `RedundantReturnKeyword`: finds a `return` in a branch of an `if`, `when` or elvis expression that is already returned.
- `intellij-kotlin-redundant-constructs`: `RedundantReturnLabel`: finds a label on a `return` that targets the enclosing function, not a lambda.
- `intellij-kotlin-redundant-constructs`: `RedundantSamConstructor`: finds a SAM constructor call in an argument where a plain lambda converts to the same type.
- `intellij-kotlin-redundant-constructs`: `RedundantSetter`: finds a setter that is the default one or only assigns the value to the backing field.
- `intellij-kotlin-redundant-constructs`: `RedundantUpperBound`: finds a type parameter bound that is `Any?`, the default upper bound.
- `intellij-kotlin-redundant-constructs`: `RedundantValueArgument`: finds an argument whose constant value equals the default value of the parameter. Inactive by default.
- `intellij-kotlin-redundant-constructs`: `RedundantWith`: finds a `with` call whose lambda does not use the receiver.
- `intellij-kotlin-redundant-constructs`: `RemoveExplicitSuperQualifier`: finds a `super<T>` qualifier that does not change the member that the call resolves to.
- `intellij-kotlin-redundant-constructs`: `RemoveExplicitTypeArguments`: finds explicit type arguments that the compiler can infer.
- `intellij-kotlin-redundant-constructs`: `RemoveRedundantQualifierName`: finds a package, class or companion qualifier that the code resolves the same without.
- `intellij-kotlin-redundant-constructs`: `RemoveRedundantSpreadOperator`: finds a spread operator on an array that is created in place with an `arrayOf`-like call or a `[...]` literal.
- `intellij-kotlin-redundant-constructs`: `RemoveSetterParameterType`: finds an explicit type on a setter parameter.
- `intellij-kotlin-redundant-constructs`: `RemoveSingleExpressionStringTemplate`: finds a string template that only holds one non-null `String` expression.
- `intellij-kotlin-redundant-constructs`: `SimplifyWhenWithBooleanConstantCondition`: finds a subjectless `when` with a `true` or `false` branch condition.
- `intellij-kotlin-redundant-constructs`: `SuspiciousCallableReferenceInLambda`: finds a lambda whose only statement is a callable reference.
- `intellij-kotlin-redundant-constructs`: `UnnecessaryOptInAnnotation`: finds an `@OptIn` annotation or marker that no used API requires.
- `intellij-kotlin-redundant-constructs`: `UnnecessaryVariable`: finds a local variable that only copies another local value or parameter.
- `intellij-kotlin-redundant-constructs`: `UnusedContextParameterCall`: finds a `context(...)` call whose block does not use all the arguments.
- `intellij-kotlin-redundant-constructs`: `UnusedReceiverParameter`: finds an extension receiver that is never used.
- `intellij-kotlin-redundant-constructs`: `VariableInitializerIsRedundant`: finds a variable initializer that is overwritten before it is read.
- `intellij-kotlin-redundant-constructs`: `WhenWithOnlyElse`: finds a `when` with only an `else` branch. Inactive by default.
- `intellij-kotlin-style-issues`: `AddOperatorModifier`: finds a function that matches an operator convention but has no `operator` modifier.
- `intellij-kotlin-style-issues`: `AddVarianceModifier`: finds a class type parameter that is used only in input or only in output positions and can be `in` or `out`.
- `intellij-kotlin-style-issues`: `BooleanLiteralArgument`: finds adjacent `true` or `false` arguments without parameter names.
- `intellij-kotlin-style-issues`: `CascadeIf`: finds an `if`-`else if` chain on one subject that `when` can replace.
- `intellij-kotlin-style-issues`: `CollectionConcatenationToBuildCollection`: finds a chain of two or more `+` or `-` operations on a list or set that `buildList` or `buildSet` can replace.
- `intellij-kotlin-style-issues`: `ConvertPairConstructorToToFunction`: finds an explicit `Pair(a, b)` constructor call that can be the infix `a to b`. Opposite of `InfixCallToOrdinary`.
- `intellij-kotlin-style-issues`: `ConvertRangeCheckToTwoComparisons`: finds an `in` or `!in` check of simple values against a standard range that can be two comparisons. Opposite of `ConvertTwoComparisonsToRangeCheck`, which is active by default. Inactive by default.
- `intellij-kotlin-style-issues`: `ConvertReferenceToLambda`: finds a callable reference that a lambda can replace.
- `intellij-kotlin-style-issues`: `ConvertSealedClassToSealedInterface`: finds a sealed class without state, constructor parameters or final members that can be a sealed interface. Opposite of `ConvertSealedInterfaceToSealedClass`.
- `intellij-kotlin-style-issues`: `ConvertSealedInterfaceToSealedClass`: finds a sealed interface whose inheritors are all plain classes or objects that can extend a sealed class. Opposite of `ConvertSealedClassToSealedInterface`, which is active by default. Inactive by default.
- `intellij-kotlin-style-issues`: `ConvertSecondaryConstructorToPrimary`: finds a secondary constructor that every other constructor delegates to and that can be the primary constructor.
- `intellij-kotlin-style-issues`: `ConvertToExplicitBackingFields`: finds a property whose getter only returns a private property of a narrower type, which an explicit backing field can replace. Opposite of `RevertExplicitBackingFields`.
- `intellij-kotlin-style-issues`: `ConvertToStringTemplate`: finds a `String` concatenation of literals and simple values that a string template can replace.
- `intellij-kotlin-style-issues`: `ConvertTryFinallyToUseCall`: finds a `try`-`finally` that only closes a `Closeable` in `finally`, which `use()` can replace.
- `intellij-kotlin-style-issues`: `ConvertTwoComparisonsToRangeCheck`: finds two comparisons of one value with a lower and an upper bound that an `in` or `!in` range check can replace. Opposite of `ConvertRangeCheckToTwoComparisons`.
- `intellij-kotlin-style-issues`: `CopyWithoutNamedArguments`: finds a data class `copy` call that passes arguments without names.
- `intellij-kotlin-style-issues`: `DestructuringDeclaration`: finds a local variable, loop variable or lambda parameter of a data class or `Map.Entry` type that is only used to read its components.
- `intellij-kotlin-style-issues`: `FilterIsInstanceCallWithClassLiteralArgument`: finds a `filterIsInstance(X::class.java)` call that `filterIsInstance<X>()` can replace.
- `intellij-kotlin-style-issues`: `FoldInitializerAndIfToElvis`: finds an `if` null or type check that exits right after a variable declaration and can fold into the initializer with `?:`.
- `intellij-kotlin-style-issues`: `IfThenToElvis`: finds an `if` that checks a value for `null` or a type and that the elvis operator `?:` can replace.
- `intellij-kotlin-style-issues`: `IfThenToSafeAccess`: finds an `if` that checks a value for `null` or a type, returns `null` otherwise, and that `?.` or `as?` can replace.
- `intellij-kotlin-style-issues`: `ImplicitThis`: finds a member access or callable reference that uses an implicit `this` receiver. Opposite of `ExplicitThis`, which is active by default. Inactive by default.
- `intellij-kotlin-style-issues`: `IntroduceWhenSubject`: finds a `when` without a subject whose branches all check the same value, which can be the subject.
- `intellij-kotlin-style-issues`: `JavaMapForEach`: finds a call of the Java `Map.forEach` with a two-parameter lambda instead of the Kotlin `forEach` with a destructured entry.
- `intellij-kotlin-style-issues`: `JoinDeclarationAndAssignment`: finds a property without an initializer that can join its first assignment.
- `intellij-kotlin-style-issues`: `LiftReturnOrAssignment`: finds an `if`, `when` or `try` whose branches all end in a return or in an assignment to the same variable.
- `intellij-kotlin-style-issues`: `MapToForEach`: finds a `map`-like call whose result is not used and that `forEach` or `forEachIndexed` can replace.
- `intellij-kotlin-style-issues`: `MoveLambdaOutsideParentheses`: finds a lambda passed as the last argument inside the parentheses that can be a trailing lambda.
- `intellij-kotlin-style-issues`: `MoveVariableDeclarationIntoWhen`: finds a variable declared right before a `when` and used only in it, which can move into the `when` subject.
- `intellij-kotlin-style-issues`: `NullableHashCode`: finds an `x?.hashCode() ?: 0` on a nullable receiver that `x.hashCode()` can replace.
- `intellij-kotlin-style-issues`: `RedundantAsSequence`: finds an `asSequence()` call on a sequence, or on an iterable followed by one terminal operation.
- `intellij-kotlin-style-issues`: `RedundantElseInIf`: finds an `else` branch after `if` branches that all end with a jump such as `return` or `throw`.
- `intellij-kotlin-style-issues`: `RedundantObjectTypeCheck`: finds an `is` check against a non-data object type that should be `===` or `!==`.
- `intellij-kotlin-style-issues`: `RedundantRunCatching`: finds a `runCatching { }.getOrThrow()` chain that `run { }` can replace.
- `intellij-kotlin-style-issues`: `RemoveEmptyParenthesesFromAnnotationEntry`: finds empty parentheses after an annotation that needs no arguments.
- `intellij-kotlin-style-issues`: `ReplaceAddAllWithMapTo`: finds an `addAll` or `+=` of a `map` or `filter` result that `mapTo` or `filterTo` can replace.
- `intellij-kotlin-style-issues`: `ReplaceAssertBooleanWithAssertEquality`: finds an `assertTrue` or `assertFalse` call with an `==` or `===` check that an equality assertion can replace.
- `intellij-kotlin-style-issues`: `ReplaceAssociateFunction`: finds an `associate` or `associateTo` call that `associateBy` or `associateWith` can replace.
- `intellij-kotlin-style-issues`: `ReplaceCallWithBinaryOperator`: finds an explicit call of `equals`, `compareTo` or an arithmetic or range operator function that a binary operator can replace.
- `intellij-kotlin-style-issues`: `ReplaceCollectionCountWithSize`: finds a `count()` call without a predicate on a collection, array or map that `size` can replace. Inactive by default.
- `intellij-kotlin-style-issues`: `ReplaceContains`: finds a call of an operator `contains` function that the `in` operator can replace.
- `intellij-kotlin-style-issues`: `ReplaceIfExpressionWithFirstOrNull`: finds an `if` that returns the first element of a non-empty collection, string or array, else `null`, which `firstOrNull()` can replace.
- `intellij-kotlin-style-issues`: `ReplaceJavaStaticMethodWithKotlinAnalog`: finds a call of a Java static method that has a Kotlin standard library counterpart.
- `intellij-kotlin-style-issues`: `ReplaceManualRangeWithIndicesCalls`: finds a manual range from `0` to the size or last index that `indices`, a loop over the elements or `withIndex()` can replace.
- `intellij-kotlin-style-issues`: `ReplaceMapGetOrDefault`: finds a `map.getOrDefault(key, default)` call on a map with non-null values that `map[key] ?: default` can replace.
- `intellij-kotlin-style-issues`: `ReplaceMapIndexedWithListGenerator`: finds a `mapIndexed` call on a collection that ignores the element, where `List(size) { index -> ... }` fits.
- `intellij-kotlin-style-issues`: `ReplaceMapKeysCallChainWithKeys`: finds a `map { it.key }.toSet()` call chain on a map that `keys` can replace.
- `intellij-kotlin-style-issues`: `ReplaceNegatedIsEmptyWithIsNotEmpty`: finds a negated `isEmpty`, `isNotEmpty`, `isBlank` or `isNotBlank` call that the inverted function can replace.
- `intellij-kotlin-style-issues`: `ReplaceNotNullAssertionWithElvisReturn`: finds a `!!` in a function or labeled lambda that returns `Unit` or a nullable type, where `?: return` fits. Inactive by default.
- `intellij-kotlin-style-issues`: `ReplaceRangeStartEndInclusiveWithFirstLast`: finds a `start` or `endInclusive` on a primitive range that the unboxed `first` or `last` can replace.
- `intellij-kotlin-style-issues`: `ReplaceReadLineWithReadln`: finds a `readLine()` call that `readln()` or `readlnOrNull()` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSizeCheckWithIsNotEmpty`: finds a size, length or count comparison that `isNotEmpty()` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSizeZeroCheckWithIsEmpty`: finds a size, length or count zero check that `isEmpty()` can replace.
- `intellij-kotlin-style-issues`: `ReplaceStringFormatWithLiteral`: finds a `String.format` call with only `%s` placeholders that a string template can replace.
- `intellij-kotlin-style-issues`: `ReplaceSubstringWithDropLast`: finds an `s.substring(0, s.length - n)` call that `s.dropLast(n)` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSubstringWithIndexingOperation`: finds a `substring(i, i + 1)` call with constant indices that `s[i]` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSubstringWithSubstringAfter`: finds an `s.substring(s.indexOf(x))` call that `s.substringAfter(x)` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSubstringWithSubstringBefore`: finds an `s.substring(0, s.indexOf(x))` call that `s.substringBefore(x)` can replace.
- `intellij-kotlin-style-issues`: `ReplaceSubstringWithTake`: finds an `s.substring(0, n)` call that `s.take(n)` can replace.
- `intellij-kotlin-style-issues`: `ReplaceToStringWithStringTemplate`: finds a `toString()` call on a reference that a string template can replace.
- `intellij-kotlin-style-issues`: `ReplaceToWithInfixForm`: finds a dot call of the infix `to` function that can use the infix form. Opposite of `InfixCallToOrdinary`.
- `intellij-kotlin-style-issues`: `ReplaceUntilWithRangeUntil`: finds an infix `until` call from the standard library that the `..<` operator can replace.
- `intellij-kotlin-style-issues`: `ReplaceWithCallWithContextCall`: finds a `with` call whose receiver is only used as a context argument, so a `context` call fits.
- `intellij-kotlin-style-issues`: `ReplaceWithIgnoreCaseEquals`: finds an equality check of two identical case conversions that `equals(..., ignoreCase = true)` can replace.
- `intellij-kotlin-style-issues`: `ReplaceWithImportAlias`: finds a qualified name that an existing import alias can replace.
- `intellij-kotlin-style-issues`: `ReplaceWithOperatorAssignment`: finds an assignment such as `x = x + y` that an operator assignment such as `x += y` can replace.
- `intellij-kotlin-style-issues`: `RevertExplicitBackingFields`: finds a property with an explicit backing field that a private backing property can replace. Opposite of `ConvertToExplicitBackingFields`, which is active by default. Inactive by default.
- `intellij-kotlin-style-issues`: `SafeCastWithReturn`: finds an `x as? T ?: return` statement that an `if (x !is T) return` check can replace.
- `intellij-kotlin-style-issues`: `SimplifiableCall`: finds a `flatMap`, `filter` or `mapNotNull` call with a trivial lambda that `flatten`, `filterNotNull` or `filterIsInstance` can replace.
- `intellij-kotlin-style-issues`: `SimplifiableCallChain`: finds a collection, sequence or text call chain that one call can replace.
- `intellij-kotlin-style-issues`: `SimplifyAssertNotNull`: finds an `assert(x != null)` right after the declaration of `x` that can fold into the initializer.
- `intellij-kotlin-style-issues`: `SimplifyBooleanWithConstants`: finds a boolean expression with a constant operand that can be simpler, such as `x && true`.
- `intellij-kotlin-style-issues`: `SimplifyNegatedBinaryExpression`: finds a negated comparison, `is` or `in` check, or boolean literal that the inverted operator can replace.
- `intellij-kotlin-style-issues`: `SimplifyNestedEachInScopeFunction`: finds an `also` or `apply` whose only statement is `forEach` or `onEach` on the receiver, which `onEach` can replace.
- `intellij-kotlin-style-issues`: `UnclearPrecedenceOfBinaryExpression`: finds an expression that mixes `?:` with equality, comparison, `in`, `is`, arithmetic or infix calls, or `||` in a `when` guard, without parentheses.
- `intellij-kotlin-style-issues`: `UnlabeledReturnInsideLambda`: finds an unlabeled `return` inside an inline lambda that returns from the enclosing function.
- `intellij-kotlin-style-issues`: `UnsafeCastWithReturn`: finds an unsafe cast followed by `?: return` that should be the safe cast `as?`.
- `intellij-kotlin-style-issues`: `UseExpressionBody`: finds a function or property accessor whose block body holds only a `return` or one `Unit` or `Nothing` expression.
- `intellij-kotlin-style-issues`: `UsePropertyAccessSyntax`: finds a call of a Java getter or setter that Kotlin property access syntax can replace.
- `intellij-kotlin-style-issues`: `VerboseNullabilityAndEmptiness`: finds a null check followed by an emptiness or blankness check that `isNullOrEmpty()` or `isNullOrBlank()` can replace.

### Distribution

- One jar per rule set, named `intellij-inspections-<rule-set>-<version>.jar`, is attached to the
  GitHub release. Each jar contains a default config. Use it with `buildUponDefaultConfig = true`.
- Bundle jars `intellij-inspections-android-all-<version>.jar`,
  `intellij-inspections-jvm-all-<version>.jar` and `intellij-inspections-kotlin-all-<version>.jar`
  contain all rule sets of one kind. Do not add a bundle jar and its rule set jars together.
- The rules require detekt 2 (Gradle plugin `dev.detekt`).
