# intellij-inspections-detekt-rules

[![Build](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml/badge.svg)](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml)

[detekt](https://detekt.dev) rules ported from the Kotlin, JVM, Android, Gradle and Compose
Multiplatform inspections of IntelliJ IDEA. They give the same hints outside the IDE, e.g. to AI
agents and CI.

Each bundle module puts all rule sets of one kind (Android, JVM or Kotlin) in one jar. The
Gradle and Compose Multiplatform kinds have only one rule set each, so they have no bundle.

| Module | Rule sets | Jar |
|---|---|---|
| `bundles:android` | All `intellij-android-*` rule sets | `intellij-inspections-android-all-<version>.jar` |
| `bundles:jvm` | All `intellij-jvm-*` rule sets | `intellij-inspections-jvm-all-<version>.jar` |
| `bundles:kotlin` | All `intellij-kotlin-*` rule sets | `intellij-inspections-kotlin-all-<version>.jar` |

Each rule set module is one inspection group of the IDE settings (Editor | Inspections | Kotlin,
Editor | Inspections | JVM languages, a group of Editor | Inspections | Java, an Android group
such as Editor | Inspections | Compose preview, a group of Editor | Inspections | Gradle, or
Editor | Inspections | Compose Multiplatform Preview).

| Module | Rule set id | Jar |
|---|---|---|
| `rules:android:compose-preview` | `intellij-android-compose-preview` | `intellij-inspections-android-compose-preview-<version>.jar` |
| `rules:android:glance-preview` | `intellij-android-glance-preview` | `intellij-inspections-android-glance-preview-<version>.jar` |
| `rules:android:kotlin` | `intellij-android-kotlin` | `intellij-inspections-android-kotlin-<version>.jar` |
| `rules:android:wear-tile-preview` | `intellij-android-wear-tile-preview` | `intellij-inspections-android-wear-tile-preview-<version>.jar` |
| `rules:compose:multiplatform-preview` | `intellij-compose-multiplatform-preview` | `intellij-inspections-compose-multiplatform-preview-<version>.jar` |
| `rules:gradle:best-practices` | `intellij-gradle-best-practices` | `intellij-inspections-gradle-best-practices-<version>.jar` |
| `rules:jvm:code-maturity` | `intellij-jvm-code-maturity` | `intellij-inspections-jvm-code-maturity-<version>.jar` |
| `rules:jvm:declaration-redundancy` | `intellij-jvm-declaration-redundancy` | `intellij-inspections-jvm-declaration-redundancy-<version>.jar` |
| `rules:jvm:general` | `intellij-jvm-general` | `intellij-inspections-jvm-general-<version>.jar` |
| `rules:jvm:junit` | `intellij-jvm-junit` | `intellij-inspections-jvm-junit-<version>.jar` |
| `rules:jvm:logging` | `intellij-jvm-logging` | `intellij-inspections-jvm-logging-<version>.jar` |
| `rules:jvm:test-frameworks` | `intellij-jvm-test-frameworks` | `intellij-inspections-jvm-test-frameworks-<version>.jar` |
| `rules:kotlin:code-migration` | `intellij-kotlin-code-migration` | `intellij-inspections-kotlin-code-migration-<version>.jar` |
| `rules:kotlin:coroutines` | `intellij-kotlin-coroutines` | `intellij-inspections-kotlin-coroutines-<version>.jar` |
| `rules:kotlin:java-interop` | `intellij-kotlin-java-interop` | `intellij-inspections-kotlin-java-interop-<version>.jar` |
| `rules:kotlin:logging` | `intellij-kotlin-logging` | `intellij-inspections-kotlin-logging-<version>.jar` |
| `rules:kotlin:naming-conventions` | `intellij-kotlin-naming-conventions` | `intellij-inspections-kotlin-naming-conventions-<version>.jar` |
| `rules:kotlin:numeric-issues` | `intellij-kotlin-numeric-issues` | `intellij-inspections-kotlin-numeric-issues-<version>.jar` |
| `rules:kotlin:other-problems` | `intellij-kotlin-other-problems` | `intellij-inspections-kotlin-other-problems-<version>.jar` |
| `rules:kotlin:probable-bugs` | `intellij-kotlin-probable-bugs` | `intellij-inspections-kotlin-probable-bugs-<version>.jar` |
| `rules:kotlin:redundant-constructs` | `intellij-kotlin-redundant-constructs` | `intellij-inspections-kotlin-redundant-constructs-<version>.jar` |
| `rules:kotlin:style-issues` | `intellij-kotlin-style-issues` | `intellij-inspections-kotlin-style-issues-<version>.jar` |

## Use in a project

The project must use detekt 2 (Gradle plugin `dev.detekt`).

1. Download the jars from
   [GitHub Releases](https://github.com/overpas/intellij-inspections-detekt-rules/releases) into
   a directory of your project, for example `config/detekt/plugins/`. Download one of these:
   - For all Kotlin rule sets: `intellij-inspections-kotlin-all-<version>.jar`.
   - For all JVM rule sets: `intellij-inspections-jvm-all-<version>.jar`.
   - For all Android rule sets: `intellij-inspections-android-all-<version>.jar`.
   - For some rule sets only: their jars from the tables above.

   Do not add a bundle jar and its rule set jars together. Detekt then loads the same rule sets
   two times.
2. In `build.gradle.kts`, add the jars to the detekt plugins and set
   `buildUponDefaultConfig = true`. The example uses `config/detekt/plugins/`. Change the path to
   your directory:

   ```kotlin
   dependencies {
       detektPlugins(fileTree(rootProject.layout.projectDirectory.dir("config/detekt/plugins")) { include("*.jar") })
   }

   detekt {
       buildUponDefaultConfig = true
   }
   ```

   Each jar has a default config. Without `buildUponDefaultConfig = true`, detekt does not use it.
3. Optional: to change a rule, write only the change in your detekt config. For example, to
   activate a rule that is "Inactive by default" in [Rules](#rules):

   ```yaml
   intellij-kotlin-numeric-issues:
     KotlinBigDecimalEquals:
       active: true
   ```

4. Run the detekt tasks with type resolution:

   ```shell
   ./gradlew detektMain detektTest
   ```

   Most rules need type resolution. The plain `detekt` task does not have it, so it skips these
   rules and does not report this. In a Kotlin Multiplatform project, run the tasks of all
   targets, for example `detektMainJvm`, `detektTestJvm`, `detektMainIosArm64` and
   `detektMainJs`. `./gradlew tasks --all` lists them.

## Rules

Every rule takes the standard detekt options (`active`, `excludes`, `includes`, ...). The Options
column gives the other options of a rule. The rule names are the short names of the IntelliJ
inspections.

Each rule set jar contains a default config (`config/config.yml`). The rules that IntelliJ IDEA
disables by default are inactive, and so are some rules that give noisy findings in CI. Of two opposite rules, only the one that is more idiomatic for
Kotlin is active. detekt uses this default config with `buildUponDefaultConfig = true`.

### `intellij-android-compose-preview`

| Rule | Finds | Options |
|---|---|---|
| `ComposePreviewDimensionRespectsLimit` | A `@Preview` whose `widthDp` or `heightDp` is not between 1 and 3000. | |
| `ComposePreviewMustBeTopLevelFunction` | A function with `@Preview` or a MultiPreview annotation that is not top level and is not a non-nested function of a top-level class with a no-parameter constructor. | |
| `ComposePreviewNeedsComposableAnnotation` | A function with `@Preview` or a MultiPreview annotation that is not `@Composable`. | |
| `PreviewAnnotationInFunctionWithParameters` | A function with `@Preview` or a MultiPreview annotation that has a parameter with no default value and no `@PreviewParameter`. | |
| `PreviewApiLevelMustBeValid` | A `@Preview` whose `apiLevel` is outside the supported range. | `minApiLevel` (default `22`): the lowest supported API level.<br>`maxApiLevel` (default `37`): the highest supported API level. |
| `PreviewDeviceShouldUseNewSpec` | A `@Preview` whose `device` is a legacy device spec that starts with `spec:id`, such as the old `Devices.PIXEL_4`. | |
| `PreviewFontScaleMustBeGreaterThanZero` | A `@Preview` whose `fontScale` is zero or negative. | |
| `PreviewMultipleParameterProviders` | A function with `@Preview` or a MultiPreview annotation that has more than one `@PreviewParameter`. | |
| `PreviewParameterProviderOnFirstParameter` | A function with `@Preview` or a MultiPreview annotation that has its first `@PreviewParameter` on a parameter other than the first. | |
| `PreviewShouldNotBeCalledRecursively` | A call inside a function with `@Preview` or a MultiPreview annotation to a function with the same name as that preview. Inactive by default. | |
| `PreviewWrapperUsage` | A `@PreviewWrapper` on a function without both `@Composable` and `@Preview` (or a MultiPreview), a repeated `@PreviewWrapper` on an annotation class, or a composable function or annotation class with more than one `@PreviewWrapper` in its annotation graph. | |

### `intellij-android-glance-preview`

| Rule | Finds |
|---|---|
| `GlancePreviewDimensionRespectsLimit` | A Glance `@Preview` annotation on a function or an annotation class with a constant `widthDp` or `heightDp` value outside 1..3000. |
| `GlancePreviewMustBeTopLevelFunction` | A Glance `@Preview` or MultiPreview annotation on a function that is not top level and not in a top level class with a default constructor. |
| `GlancePreviewNeedsComposableAnnotation` | A Glance `@Preview` or MultiPreview annotation on a function that is not `@Composable`. |

### `intellij-android-kotlin`

| Rule | Finds |
|---|---|
| `IllegalIdentifier` | A backticked identifier with characters that the dex format does not allow in a simple name, for example a space or a dot. These names fail on Android versions before 11. By default, unit test and screenshot test source sets are excluded (`excludes`). |

### `intellij-android-wear-tile-preview`

| Rule | Finds |
|---|---|
| `WearTilePreviewComposableAnnotationIsNotSupported` | A `@Composable` annotation on a Tile preview function. |
| `WearTilePreviewContextComesFromParameter` | A `Context` in a Tile preview function that does not come from the `Context` parameter of the function. |
| `WearTilePreviewHasValidMethodSignature` | A function with a Tile `@Preview` annotation that does not return `TilePreviewData`, or has parameters other than one `Context`. |
| `WearTilePreviewMethodIsAnnotatedWithTilePreviewAnnotation` | A `Preview` annotation from another package, or a MultiPreview annotation that refers to one, on a function with the Tile preview signature. |
| `WearTilePreviewShouldBeTopLevelFunction` | A function with a Tile `@Preview` annotation in another function, in a nested class, or in a class without a default constructor. |

### `intellij-compose-multiplatform-preview`

These rules check the Jetpack (`androidx.compose.ui.tooling.preview`) and the Compose Multiplatform
(`org.jetbrains.compose.ui.tooling.preview`) `@Preview` and `@PreviewParameter` annotations. A
MultiPreview annotation counts only on a `@Composable` function.

| Rule | Finds |
|---|---|
| `MultiplatformPreviewAnnotationInFunctionWithParameters` | A preview function that has a parameter with no default value and no `@PreviewParameter`. |
| `MultiplatformPreviewMultipleParameterProviders` | A preview function that has more than one `@PreviewParameter`. |

### `intellij-gradle-best-practices`

These rules check only Gradle Kotlin DSL scripts (`*.gradle.kts`). detekt checks a script only if
it is in the `source` of the detekt task, for example
`source.from(layout.projectDirectory.file("build.gradle.kts"))`. The rules use only the syntax of
the script, because detekt does not have the Gradle model.

| Rule | Finds |
|---|---|
| `AvoidApplyPluginMethod` | An `apply(plugin = ...)` or `apply<T>()` call in place of the `plugins` block. |
| `AvoidDependencyNamedArgumentsNotation` | A dependency in a `dependencies` block with a named `group` argument or with positional string coordinates, in place of one `group:name:version` string. |
| `AvoidDuplicateDependencies` | A dependency that the top-level `dependencies` blocks declare more than one time in the same configuration. It does not check the configurations that extend other configurations. |
| `AvoidDuplicateRepositories` | A repository that one `repositories` block declares more than one time. Whitespace and comments do not count. |
| `AvoidRepositoriesInBuildGradle` | A `repositories` block in a `build.gradle.kts` script, except in `publishing`. Inactive by default. |
| `RedundantKotlinStdLibDependency` | A `kotlin-stdlib` dependency with the version of the `kotlin("jvm")` plugin, which adds this dependency itself. It does not read `kotlin.stdlib.default.dependency` from `gradle.properties`. |
| `TaskMissingDescription` | A task that `tasks.register`, `tasks.create`, `tasks.registering` or `tasks.creating` makes without a `description`. |

### `intellij-jvm-code-maturity`

| Rule | Finds | Options |
|---|---|---|
| `MarkedForRemoval` | A usage of an API that is deprecated with `@Deprecated(forRemoval = true)`, or an override of a method marked for removal. | `isSameOutermostClassIgnored` (default `true`): do not report the usages in the same outermost class. |

### `intellij-jvm-declaration-redundancy`

| Rule | Finds | Options |
|---|---|---|
| `SameParameterValue` | A parameter of a function that gets the same constant value in every call, when all calls are in the same file. | `highestModifier` (default `protected`): the highest visibility to check.<br>`minimalUsageCount` (default `1`): the number of calls needed to report. |
| `SameReturnValue` | A function that cannot be overridden and that always returns the same constant value. | |

### `intellij-jvm-general`

| Rule | Finds | Options |
|---|---|---|
| `BlockingMethodInNonBlockingContext` | A possibly blocking call, such as `Thread.sleep()`, a JDK method that throws `IOException` or a method annotated with `@Blocking`, in a non-blocking context such as a suspend function, a coroutine on a non-IO dispatcher or a method annotated with `@NonBlocking`. | `isUnknownContextBlocking` (default `true`): do not report the calls in contexts that are not known.<br>`isSuspendContextNonBlocking` (default `true`): consider suspend functions and suspend lambdas non-blocking.<br>`blockingAnnotations` (default the `Blocking` annotations of JetBrains, Micronaut and SmallRye): more annotations that mark blocking methods. The rule always uses the default ones.<br>`nonBlockingAnnotations` (default the `NonBlocking` annotations of JetBrains, Micronaut and SmallRye): more annotations that mark non-blocking contexts. The rule always uses the default ones. |
| `MissingDeprecatedAnnotationOnScheduledForRemovalApi` | A declaration marked with `@ApiStatus.ScheduledForRemoval` that is not also marked with `@Deprecated`. | |
| `MustAlreadyBeRemovedApi` | A declaration marked with `@ApiStatus.ScheduledForRemoval` for a version that is not later than the current version. | `currentVersion` (default empty, which turns the rule off): the current version. |
| `NonExtendableApiUsage` | A class, interface or override that extends, implements or overrides a library API marked with `@ApiStatus.NonExtendable`, or the annotation on a declaration that cannot be extended. | |
| `OverrideOnly` | A call or reference of a library method marked with `@ApiStatus.OverrideOnly` outside a super or delegate call in its override, or the annotation on a declaration that cannot be overridden. | |
| `SerializableHasSerialVersionUIDField` | A `Serializable` class without a `serialVersionUID` field. Inactive by default. | `isAnonymousClassIgnored` (default `false`): do not report anonymous objects.<br>`ignoredSuperClasses` (default `java.awt.Component`): do not report the subclasses of these classes that do not implement `Serializable` directly. |
| `SuppressionAnnotation` | A `@Suppress` or `@SuppressWarnings` annotation or a `//noinspection` comment that suppresses inspections. Inactive by default. | `allowedSuppressions` (default empty): the suppressed ids to accept. |
| `SystemGetProperty` | A `System.getProperty()` call for `file.separator`, `path.separator`, `line.separator` or `file.encoding`, which a dedicated API gives directly. | |
| `ThreadRun` | A call of `run()` on a `Thread`, which runs the code in the current thread instead of starting a new one. | |
| `UnstableApiUsage` | A usage of a library API that an unstable API annotation such as `@ApiStatus.Experimental` or `@ApiStatus.Internal` marks, or whose signature refers to such a type. | `unstableApiAnnotations` (default the unstable API annotations of JetBrains, Guava, RxJava, Apache HttpClient and Gradle): the annotations that mark unstable APIs.<br>`isInsideImportsIgnored` (default `true`): do not report imports.<br>`isApiDeclaredInThisProjectIgnored` (default `true`): report only compiled library APIs. |
| `UnstableTypeUsedInSignature` | A public declaration whose signature refers to a type marked with an unstable API annotation, but that does not have the same annotation itself. Inactive by default. | `unstableApiAnnotations` (default as in `UnstableApiUsage`): the annotations that mark unstable APIs. |
| `UrlHashCode` | An `equals()` or `hashCode()` call or an `==` comparison on `java.net.URL`, or a map or set of URLs, which can resolve the host over the network. | |

### `intellij-jvm-junit`

| Rule | Finds | Options |
|---|---|---|
| `AssertEqualsCalledOnArray` | An `assertEquals()` call on two arrays, which compares them by identity and not by content. Inactive by default. | |
| `AssertEqualsMayBeAssertSame` | An `assertEquals()` call on two objects of a final class that does not override `equals()`, where `assertSame()` fits. Inactive by default. | |
| `IgnoredJUnitTest` | A test class or test method that `@Ignore` or `@Disabled` turns off. Inactive by default. | `isReasonAccepted` (default `true`): do not report the ones that give a reason. |
| `JUnit3StyleTestMethodInJUnit4Class` | A `test...` method without `@Test` in a JUnit 4 class, which JUnit 4 does not run. | |
| `JUnit5AssertionsConverter` | A JUnit 3 or JUnit 4 assertion or assumption in a JUnit 5 test. | |
| `JUnitMalformedDeclaration` | A JUnit test class, test method, lifecycle method, rule, extension or parameter source with a declaration that JUnit cannot run or rejects. | `ignorableAnnotations` (default `mockit.Mocked`, `org.junit.jupiter.api.io.TempDir`): the parameter annotations to accept.<br>`areSuspendFunctionsAllowed` (default `false`): accept suspend test and lifecycle functions. |
| `JUnitMixedFramework` | A test class that uses annotations of a JUnit version other than the version of the class. | |
| `Junit4RunWithInspection` | A `@RunWith` on a test class whose superclass already has `@RunWith`. | |
| `MigrateAssertToMatcherAssert` | A JUnit assertion such as `assertEquals()` that a Hamcrest `assertThat()` can replace, when Hamcrest is on the classpath. Inactive by default. | |
| `ParameterizedParametersStaticCollection` | A JUnit 4 `Parameterized` test class with no `@Parameters` method, more than one, or one with a bad signature. Inactive by default. | |
| `SuperTearDownInFinally` | A `super.tearDown()` call in a JUnit 3 `tearDown()` that is not in a `finally` block. Inactive by default. | |
| `UseOfObsoleteAssert` | A call of the JUnit 3 `junit.framework.Assert` or `TestCase` assertions, when JUnit 4 or later is on the classpath. Inactive by default. | |

### `intellij-jvm-logging`

| Rule | Finds | Options |
|---|---|---|
| `LogStatementNotGuardedByLogCondition` | A logging call with computed arguments that a log level condition such as `if (logger.isDebugEnabled)` does not guard. Inactive by default. | `limitLevelType` (default `DEBUG_AND_LOWER`): the log levels to check, `ALL`, `WARN_AND_LOWER`, `INFO_AND_LOWER`, `DEBUG_AND_LOWER` or `TRACE`.<br>`isUnguardedConstantFlagged` (default `false`): also report the calls with only constant arguments. |
| `LoggingConditionDisagreesWithLogLevelStatement` | A log level condition whose level does not agree with the level of the logging call that it guards. Inactive by default. | |
| `LoggingPlaceholderCountMatchesArgumentCount` | A logging call whose number of placeholders does not match the number of arguments. | `slf4jToLog4J2Type` (default `AUTO`): tells if SLF4J calls go to Log4j 2, `AUTO`, `YES` or `NO`. |
| `LoggingStringTemplateAsArgument` | A logging call that builds its message with a string template or concatenation instead of placeholders. | `limitLevelType` (default `DEBUG_AND_LOWER`): the log levels to check, `ALL`, `WARN_AND_LOWER`, `INFO_AND_LOWER`, `DEBUG_AND_LOWER` or `TRACE`.<br>`arePrimitivesSkipped` (default `true`): do not report templates with only primitive values.<br>`isOnlyExceptionSkipped` (default `false`): do not report the calls whose only other argument is an exception. |

### `intellij-jvm-test-frameworks`

| Rule | Finds | Options |
|---|---|---|
| `AssertBetweenInconvertibleTypes` | A JUnit, TestNG or AssertJ equality or identity assertion between objects of inconvertible types. | |
| `TestCaseWithConstructor` | A test class that does initialization work in a constructor or an `init` block instead of a set-up method. Inactive by default. | |
| `TestCaseWithNoTestMethods` | A test class without test methods. Inactive by default. | `isSupersIgnored` (default `true`): do not report a class when a superclass has test methods. |
| `TestMethodWithoutAssertion` | A test method without assertions. Inactive by default. | `assertionMethods` (default the assertions of JUnit, kotlin.test, TestNG, AssertJ, Truth, Hamcrest, Mockito, MockK and JMockit): the assertion methods as `<class or package>#<method name regex>`.<br>`isAssertKeywordAssertion` (default `true`): count `assert()` as an assertion.<br>`isThrowingIgnored` (default `false`): do not report a method with `@Throws`. |

### `intellij-kotlin-code-migration`

| Rule | Finds |
|---|---|
| `CanConvertToMultiDollarString` | A string that escapes two or more dollar characters (`\$`), or writes one as `${'$'}`, and can use an interpolation prefix instead. Each escaped dollar is reported. Unlike IntelliJ, a string whose only escape is a single `\$`, such as `"%1\$s"`, is not reported. Opposite of `ConvertFromMultiDollarToRegularString`. |
| `ConvertFromMultiDollarToRegularString` | A string with an interpolation prefix that can be a regular string. Opposite of `CanConvertToMultiDollarString`, which is active by default. Inactive by default. |
| `ConvertLongToDuration` | A kotlinx.coroutines call that uses the `Long` milliseconds overload instead of `Duration`. |
| `InfixCallToOrdinary` | An infix call that can be an ordinary call. Opposite of `ConvertPairConstructorToToFunction` and `ReplaceToWithInfixForm`, which are active by default. Inactive by default. |

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
| `UnusedFlow` | A `Flow` that is created but never collected, returned or passed on, except inside MockK verification blocks such as `verify {}`. Overlaps detekt's `IgnoredReturnValue`, which is active by default. |
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
| `KotlinLoggerInitializedWithForeignClass` | A logger that is created with the class literal of another class. Inactive by default. |

### `intellij-kotlin-naming-conventions`

| Rule | Finds |
|---|---|
| `InconsistentCommentForJavaParameter` | A `/* name = */` comment before an argument of a Java call that does not match the parameter name. |
| `LocalVariableName` | A local variable, destructuring entry or non-property parameter whose name is not lowerCamelCase with only letters and digits. |

### `intellij-kotlin-numeric-issues`

| Rule | Finds |
|---|---|
| `KotlinBigDecimalEquals` | An `equals()` or `==` comparison of `BigDecimal` values that should use `compareTo()`. Inactive by default. |

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
| `KotlinThrowableNotThrown` | A `Throwable` that a call creates or returns and that is never thrown, returned or used. By default, test source sets are excluded (`excludes`). |
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
| `ExplicitThis` | An explicit `this` receiver that the code resolves the same without. Opposite of `ImplicitThis`. |
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
| `RedundantModalityModifier` | A modality modifier that equals the default modality of the declaration. Skips `abstract` in an interface, which detekt `OptionalAbstractKeyword` reports. |
| `RedundantNullableReturnType` | A nullable return type of a function or a read-only property that never returns null. |
| `RedundantReturnKeyword` | A `return` in a branch of an `if`, `when` or elvis expression that is already returned. |
| `RedundantReturnLabel` | A label on a `return` that targets the enclosing function, not a lambda. |
| `RedundantSamConstructor` | A SAM constructor call in an argument where a plain lambda converts to the same type. |
| `RedundantSetter` | A setter that is the default one or only assigns the value to the backing field. |
| `RedundantUpperBound` | A type parameter bound that is `Any?`, the default upper bound. |
| `RedundantValueArgument` | An argument whose constant value equals the default value of the parameter. Inactive by default. |
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
| `WhenWithOnlyElse` | A `when` with only an `else` branch. Inactive by default. |

### `intellij-kotlin-style-issues`

| Rule | Finds | Options |
|---|---|---|
| `AddOperatorModifier` | A function that matches an operator convention but has no `operator` modifier. | `areSuspendFunctionsSkipped` (default `true`): do not report `suspend` functions. |
| `AddVarianceModifier` | A class type parameter that is used only in input or only in output positions and can be `in` or `out`. | |
| `BooleanLiteralArgument` | Adjacent `true` or `false` arguments without parameter names. | |
| `CascadeIf` | An `if`-`else if` chain on one subject that `when` can replace. | |
| `CollectionConcatenationToBuildCollection` | A chain of two or more `+` or `-` operations on a list or set that `buildList` or `buildSet` can replace. | |
| `ConvertPairConstructorToToFunction` | An explicit `Pair(a, b)` constructor call that can be the infix `a to b`. Opposite of `InfixCallToOrdinary`. | |
| `ConvertRangeCheckToTwoComparisons` | An `in` or `!in` check of simple values against a standard range that can be two comparisons. Opposite of `ConvertTwoComparisonsToRangeCheck`, which is active by default. Inactive by default. | |
| `ConvertReferenceToLambda` | A callable reference that a lambda can replace. Inactive by default. | |
| `ConvertSealedClassToSealedInterface` | A sealed class without state, constructor parameters, final, `internal` or `protected` members that can be a sealed interface. Opposite of `ConvertSealedInterfaceToSealedClass`. Overlaps detekt's `AbstractClassCanBeInterface` when its `ignoreSealedClasses` is `false`. | |
| `ConvertSealedInterfaceToSealedClass` | A sealed interface whose inheritors are all plain classes or objects that can extend a sealed class. Opposite of `ConvertSealedClassToSealedInterface`, which is active by default. Inactive by default. | |
| `ConvertSecondaryConstructorToPrimary` | A secondary constructor that every other constructor delegates to and that can be the primary constructor. | |
| `ConvertToExplicitBackingFields` | A property whose getter only returns a private property of a narrower type, which an explicit backing field can replace. Opposite of `RevertExplicitBackingFields`. | |
| `ConvertToStringTemplate` | A `String` concatenation of literals and simple values that a string template can replace. | |
| `ConvertTryFinallyToUseCall` | A `try`-`finally` that only closes a `Closeable` in `finally`, which `use()` can replace. | |
| `ConvertTwoComparisonsToRangeCheck` | Two comparisons of one value with a lower and an upper bound that an `in` or `!in` range check can replace. Opposite of `ConvertRangeCheckToTwoComparisons`. | |
| `CopyWithoutNamedArguments` | A data class `copy` call that passes arguments without names. | |
| `DestructuringDeclaration` | A loop variable of a data class or `Map.Entry` type that is only used to read its components. | `areNonParameterCasesReported` (default `false`): also report local variables and lambda parameters.<br>`maxDestructuringEntries` (default `3`, as in detekt's `DestructuringDeclarationWithTooManyEntries`): the maximum number of entries of the suggested declaration. |
| `FilterIsInstanceCallWithClassLiteralArgument` | A `filterIsInstance(X::class.java)` call that `filterIsInstance<X>()` can replace. | |
| `FoldInitializerAndIfToElvis` | An `if` null check that exits right after a variable declaration and can fold into the initializer with `?:`. | |
| `IfThenToElvis` | An `if` that checks a value for `null` or a type and that the elvis operator `?:` can replace. | |
| `IfThenToSafeAccess` | An `if` that checks a value for `null` or a type, returns `null` otherwise, and that `?.` or `as?` can replace. | |
| `ImplicitThis` | A member access or callable reference that uses an implicit `this` receiver. Opposite of `ExplicitThis`, which is active by default. Inactive by default. | |
| `IntroduceWhenSubject` | A `when` without a subject whose branches all check the same value, which can be the subject. | |
| `JavaMapForEach` | A call of the Java `Map.forEach` with a two-parameter lambda instead of the Kotlin `forEach` with a destructured entry. | |
| `JoinDeclarationAndAssignment` | A property without an initializer that can join its first assignment. | |
| `LiftReturnOrAssignment` | An `if`, `when` or `try` whose branches all end in a return or in an assignment to the same variable. | |
| `MapToForEach` | A `map`-like call whose result is not used and that `forEach` or `forEachIndexed` can replace. | |
| `MoveLambdaOutsideParentheses` | A lambda passed as the last argument inside the parentheses that can be a trailing lambda. | |
| `MoveVariableDeclarationIntoWhen` | A variable declared right before a `when` and used only in it, which can move into the `when` subject. | |
| `NullableHashCode` | An `x?.hashCode() ?: 0` on a nullable receiver that `x.hashCode()` can replace. | |
| `RedundantAsSequence` | An `asSequence()` call on a sequence, or on an iterable followed by one terminal operation. | |
| `RedundantElseInIf` | An `else` branch after `if` branches that all end with a jump such as `return` or `throw`. | |
| `RedundantObjectTypeCheck` | An `is` check against a non-data object type that should be `===` or `!==`. | |
| `RedundantRunCatching` | A `runCatching { }.getOrThrow()` chain that `run { }` can replace. | |
| `RemoveEmptyParenthesesFromAnnotationEntry` | Empty parentheses after an annotation that needs no arguments. | |
| `ReplaceAddAllWithMapTo` | An `addAll` or `+=` of a `map` or `filter` result that `mapTo` or `filterTo` can replace. | |
| `ReplaceAssertBooleanWithAssertEquality` | An `assertTrue` or `assertFalse` call with an `==` or `===` check that an equality assertion can replace. | |
| `ReplaceAssociateFunction` | An `associate` or `associateTo` call that `associateBy` or `associateWith` can replace. Inactive by default. | |
| `ReplaceCallWithBinaryOperator` | An explicit call of `equals` or `compareTo` that a binary operator can replace. | |
| `ReplaceCollectionCountWithSize` | A `count()` call without a predicate on a collection, array or map that `size` can replace. Inactive by default. | |
| `ReplaceContains` | A call of an operator `contains` function that the `in` operator can replace. | |
| `ReplaceIfExpressionWithFirstOrNull` | An `if` that returns the first element of a non-empty collection, string or array, else `null`, which `firstOrNull()` can replace. | |
| `ReplaceJavaStaticMethodWithKotlinAnalog` | A call of a Java static method that has a Kotlin standard library counterpart. | |
| `ReplaceManualRangeWithIndicesCalls` | A manual range from `0` to the size or last index that `indices`, a loop over the elements or `withIndex()` can replace. | |
| `ReplaceMapGetOrDefault` | A `map.getOrDefault(key, default)` call on a map with non-null values that `map[key] ?: default` can replace. | |
| `ReplaceMapIndexedWithListGenerator` | A `mapIndexed` call on a collection that ignores the element, where `List(size) { index -> ... }` fits. | |
| `ReplaceMapKeysCallChainWithKeys` | A `map { it.key }.toSet()` call chain on a map that `keys` can replace. | |
| `ReplaceNegatedIsEmptyWithIsNotEmpty` | A negated `isEmpty`, `isNotEmpty`, `isBlank` or `isNotBlank` call that the inverted function can replace. | |
| `ReplaceNotNullAssertionWithElvisReturn` | A `!!` in a function or labeled lambda that returns `Unit` or a nullable type, where `?: return` fits. Inactive by default. | |
| `ReplaceRangeStartEndInclusiveWithFirstLast` | A `start` or `endInclusive` on a primitive range that the unboxed `first` or `last` can replace. | |
| `ReplaceReadLineWithReadln` | A `readLine()` call that `readln()` or `readlnOrNull()` can replace. | |
| `ReplaceSizeCheckWithIsNotEmpty` | A size, length or count comparison that `isNotEmpty()` can replace. | |
| `ReplaceSizeZeroCheckWithIsEmpty` | A size, length or count zero check that `isEmpty()` can replace. | |
| `ReplaceStringFormatWithLiteral` | A `String.format` call with only `%s` placeholders that a string template can replace. | |
| `ReplaceSubstringWithDropLast` | An `s.substring(0, s.length - n)` call that `s.dropLast(n)` can replace. | |
| `ReplaceSubstringWithIndexingOperation` | A `substring(i, i + 1)` call with constant indices that `s[i]` can replace. | |
| `ReplaceSubstringWithSubstringAfter` | An `s.substring(s.indexOf(x))` call that `s.substringAfter(x)` can replace. | |
| `ReplaceSubstringWithSubstringBefore` | An `s.substring(0, s.indexOf(x))` call that `s.substringBefore(x)` can replace. | |
| `ReplaceSubstringWithTake` | An `s.substring(0, n)` call that `s.take(n)` can replace. | |
| `ReplaceToStringWithStringTemplate` | A `toString()` call on a reference that a string template can replace. Inactive by default. | |
| `ReplaceToWithInfixForm` | A dot call of the infix `to` function that can use the infix form. Opposite of `InfixCallToOrdinary`. | |
| `ReplaceUntilWithRangeUntil` | An infix `until` call from the standard library that the `..<` operator can replace. | |
| `ReplaceWithCallWithContextCall` | A `with` call whose receiver is only used as a context argument, so a `context` call fits. | |
| `ReplaceWithIgnoreCaseEquals` | An equality check of two identical case conversions that `equals(..., ignoreCase = true)` can replace. | |
| `ReplaceWithImportAlias` | A qualified name that an existing import alias can replace. | |
| `ReplaceWithOperatorAssignment` | An assignment such as `x = x + y` that an operator assignment such as `x += y` can replace. | |
| `RevertExplicitBackingFields` | A property with an explicit backing field that a private backing property can replace. Opposite of `ConvertToExplicitBackingFields`, which is active by default. Inactive by default. | |
| `SafeCastWithReturn` | An `x as? T ?: return` statement that an `if (x !is T) return` check can replace. | |
| `SimplifiableCall` | A `flatMap`, `filter` or `mapNotNull` call with a trivial lambda that `flatten`, `filterNotNull` or `filterIsInstance` can replace. | |
| `SimplifiableCallChain` | A collection, sequence or text call chain that one call can replace. | |
| `SimplifyAssertNotNull` | An `assert(x != null)` right after the declaration of `x` that can fold into the initializer. | |
| `SimplifyBooleanWithConstants` | A boolean expression with a constant operand that can be simpler, such as `x && true`. | |
| `SimplifyNegatedBinaryExpression` | A negated comparison, `is` or `in` check, or boolean literal that the inverted operator can replace. | |
| `SimplifyNestedEachInScopeFunction` | An `also` or `apply` whose only statement is `forEach` or `onEach` on the receiver, which `onEach` can replace. | |
| `UnclearPrecedenceOfBinaryExpression` | An expression that mixes `?:` with equality, comparison, `in`, `is`, arithmetic or infix calls, or `||` in a `when` guard, without parentheses. | |
| `UnlabeledReturnInsideLambda` | An unlabeled `return` inside an inline lambda that returns from the enclosing function. | |
| `UnsafeCastWithReturn` | An unsafe cast followed by `?: return` that should be the safe cast `as?`. | |
| `UseExpressionBody` | A function or property accessor whose block body holds only a `return` or one `Unit` or `Nothing` expression. | `areUnitExpressionsSkipped` (default `true`): do not report empty bodies and bodies with one `Unit` expression. |
| `UsePropertyAccessSyntax` | A call of a Java getter or setter that Kotlin property access syntax can replace. | |
| `VerboseNullabilityAndEmptiness` | A null check followed by an emptiness or blankness check that `isNullOrEmpty()` or `isNullOrBlank()` can replace. | |

## Build

```shell
./gradlew build      # compile, test, detekt
./gradlew koverVerify
./gradlew ruleJars   # collects the versioned jars into build/releases/<version>/
```

## Backlog

[docs/inspections.md](docs/inspections.md) lists every Kotlin inspection of IntelliJ IDEA with its
target module and a verdict on how to port it. The JVM, Android, Gradle and Compose Multiplatform
inspections are not in the catalog. Refresh the catalog with:

```shell
python3 scripts/inspection-catalog.py <intellij-community checkout> --ref master
```

Every week the [Upstream sync](.github/workflows/upstream-sync.yml) workflow runs
`scripts/inspection-sync.py`. The script compares the catalog with the `master` branch of
intellij-community and lists the new inspections, the removed inspections and the changed rules in
one issue with the `upstream-sync` label.

## License

Apache License 2.0. The rules are adapted from
[IntelliJ IDEA Community Edition](https://github.com/JetBrains/intellij-community), see
[NOTICE](NOTICE).
