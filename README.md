# intellij-inspections-detekt-rules

[![Maven Central](https://img.shields.io/maven-central/v/io.github.overpas/intellij-inspections-kotlin-all)](https://central.sonatype.com/namespace/io.github.overpas)
[![Build](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml/badge.svg)](https://github.com/overpas/intellij-inspections-detekt-rules/actions/workflows/build.yml)

[detekt](https://detekt.dev) rules ported from the Kotlin, JVM, Android, Gradle and Compose
Multiplatform inspections of IntelliJ IDEA. They give the same hints outside the IDE, e.g. to AI
agents and CI.

## Use in a project

> [!IMPORTANT]
> Requires **detekt 2** (Gradle plugin `dev.detekt`).

`build.gradle.kts`:

```kotlin
dependencies {
    val intellijInspections = "<version>"
    detektPlugins("io.github.overpas:intellij-inspections-kotlin-all:$intellijInspections")
    detektPlugins("io.github.overpas:intellij-inspections-jvm-all:$intellijInspections")
    detektPlugins("io.github.overpas:intellij-inspections-android-all:$intellijInspections")
    detektPlugins("io.github.overpas:intellij-inspections-gradle-best-practices:$intellijInspections")
    detektPlugins("io.github.overpas:intellij-inspections-compose-multiplatform-preview:$intellijInspections")
    // Or only some rule sets (see Artifacts), but not together with their bundle
}

detekt {
    buildUponDefaultConfig = true // required: applies the default config of each jar
}
```

```shell
./gradlew detektMain detektTest
```

> [!WARNING]
> **Most rules need type resolution.** The plain `detekt` task skips them silently. In Kotlin
> Multiplatform, run the tasks of all targets, e.g. `detektMainJvm`, `detektMainIosArm64`.

Optional: in the detekt config, write only the changes to the defaults.

```yaml
intellij-kotlin-numeric-issues:
  KotlinBigDecimalEquals:
    active: true # inactive by default
```

### Alternative: local jars (special cases only, e.g. no access to Maven Central)

Put the jars from [GitHub Releases](https://github.com/overpas/intellij-inspections-detekt-rules/releases)
into `config/detekt/plugins/`:

```kotlin
dependencies {
    detektPlugins(fileTree(rootProject.layout.projectDirectory.dir("config/detekt/plugins")) { include("*.jar") })
}
```

## Artifacts

Group id `io.github.overpas`, jar `<artifact id>-<version>.jar`. Gradle and Compose Multiplatform
have one rule set each, so they have no bundle.

| Module | Rule sets | Artifact id |
|---|---|---|
| `bundles:android` | All `intellij-android-*` rule sets | `intellij-inspections-android-all` |
| `bundles:jvm` | All `intellij-jvm-*` rule sets | `intellij-inspections-jvm-all` |
| `bundles:kotlin` | All `intellij-kotlin-*` rule sets | `intellij-inspections-kotlin-all` |

| Module | Rule set id | Artifact id |
|---|---|---|
| `rules:android:compose-preview` | `intellij-android-compose-preview` | `intellij-inspections-android-compose-preview` |
| `rules:android:glance-preview` | `intellij-android-glance-preview` | `intellij-inspections-android-glance-preview` |
| `rules:android:kotlin` | `intellij-android-kotlin` | `intellij-inspections-android-kotlin` |
| `rules:android:wear-tile-preview` | `intellij-android-wear-tile-preview` | `intellij-inspections-android-wear-tile-preview` |
| `rules:compose:multiplatform-preview` | `intellij-compose-multiplatform-preview` | `intellij-inspections-compose-multiplatform-preview` |
| `rules:gradle:best-practices` | `intellij-gradle-best-practices` | `intellij-inspections-gradle-best-practices` |
| `rules:jvm:code-maturity` | `intellij-jvm-code-maturity` | `intellij-inspections-jvm-code-maturity` |
| `rules:jvm:declaration-redundancy` | `intellij-jvm-declaration-redundancy` | `intellij-inspections-jvm-declaration-redundancy` |
| `rules:jvm:general` | `intellij-jvm-general` | `intellij-inspections-jvm-general` |
| `rules:jvm:junit` | `intellij-jvm-junit` | `intellij-inspections-jvm-junit` |
| `rules:jvm:logging` | `intellij-jvm-logging` | `intellij-inspections-jvm-logging` |
| `rules:jvm:test-frameworks` | `intellij-jvm-test-frameworks` | `intellij-inspections-jvm-test-frameworks` |
| `rules:kotlin:code-migration` | `intellij-kotlin-code-migration` | `intellij-inspections-kotlin-code-migration` |
| `rules:kotlin:coroutines` | `intellij-kotlin-coroutines` | `intellij-inspections-kotlin-coroutines` |
| `rules:kotlin:java-interop` | `intellij-kotlin-java-interop` | `intellij-inspections-kotlin-java-interop` |
| `rules:kotlin:logging` | `intellij-kotlin-logging` | `intellij-inspections-kotlin-logging` |
| `rules:kotlin:naming-conventions` | `intellij-kotlin-naming-conventions` | `intellij-inspections-kotlin-naming-conventions` |
| `rules:kotlin:numeric-issues` | `intellij-kotlin-numeric-issues` | `intellij-inspections-kotlin-numeric-issues` |
| `rules:kotlin:other-problems` | `intellij-kotlin-other-problems` | `intellij-inspections-kotlin-other-problems` |
| `rules:kotlin:probable-bugs` | `intellij-kotlin-probable-bugs` | `intellij-inspections-kotlin-probable-bugs` |
| `rules:kotlin:redundant-constructs` | `intellij-kotlin-redundant-constructs` | `intellij-inspections-kotlin-redundant-constructs` |
| `rules:kotlin:style-issues` | `intellij-kotlin-style-issues` | `intellij-inspections-kotlin-style-issues` |

## Rules

Every rule takes the standard detekt options (`active`, `excludes`, `includes`, ...). The Options
column gives the other options of a rule. The rule names are the short names of the IntelliJ
inspections. The IntelliJ level column gives the default level of the inspection in IntelliJ IDEA:
`ERROR`, `WARNING`, `WEAK WARNING` or `INFORMATION`. IntelliJ IDEA does not highlight an
`INFORMATION` inspection, it only offers its quick fix at the caret.

Each rule set jar contains a default config (`config/config.yml`). The rules that IntelliJ IDEA
disables by default are inactive, and so are some rules that give noisy findings in CI. Of two
opposite rules, at most one is active: the one that is more idiomatic for Kotlin.

### `intellij-android-compose-preview`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `ComposePreviewDimensionRespectsLimit` | WARNING | A `@Preview` whose `widthDp` or `heightDp` is not between 1 and 3000. | |
| `ComposePreviewMustBeTopLevelFunction` | ERROR | A function with `@Preview` or a MultiPreview annotation that is not top level and is not a non-nested function of a top-level class with a no-parameter constructor. | |
| `ComposePreviewNeedsComposableAnnotation` | ERROR | A function with `@Preview` or a MultiPreview annotation that is not `@Composable`. | |
| `PreviewAnnotationInFunctionWithParameters` | ERROR | A function with `@Preview` or a MultiPreview annotation that has a parameter with no default value and no `@PreviewParameter`. | |
| `PreviewApiLevelMustBeValid` | ERROR | A `@Preview` whose `apiLevel` is outside the supported range. | `minApiLevel` (default `22`): the lowest supported API level.<br>`maxApiLevel` (default `37`): the highest supported API level. |
| `PreviewDeviceShouldUseNewSpec` | WEAK WARNING | A `@Preview` whose `device` is a legacy device spec that starts with `spec:id`, such as the old `Devices.PIXEL_4`. | |
| `PreviewFontScaleMustBeGreaterThanZero` | ERROR | A `@Preview` whose `fontScale` is zero or negative. | |
| `PreviewMultipleParameterProviders` | ERROR | A function with `@Preview` or a MultiPreview annotation that has more than one `@PreviewParameter`. | |
| `PreviewParameterProviderOnFirstParameter` | ERROR | A function with `@Preview` or a MultiPreview annotation that has its first `@PreviewParameter` on a parameter other than the first. | |
| `PreviewShouldNotBeCalledRecursively` | WEAK WARNING | A call inside a function with `@Preview` or a MultiPreview annotation to a function with the same name as that preview. Inactive by default. | |
| `PreviewWrapperUsage` | ERROR | A `@PreviewWrapper` on a function without both `@Composable` and `@Preview` (or a MultiPreview), a repeated `@PreviewWrapper` on an annotation class, or a composable function or annotation class with more than one `@PreviewWrapper` in its annotation graph. | |

### `intellij-android-glance-preview`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `GlancePreviewDimensionRespectsLimit` | WARNING | A Glance `@Preview` annotation on a function or an annotation class with a constant `widthDp` or `heightDp` value outside 1..3000. |
| `GlancePreviewMustBeTopLevelFunction` | ERROR | A Glance `@Preview` or MultiPreview annotation on a function that is not top level and not in a top level class with a default constructor. |
| `GlancePreviewNeedsComposableAnnotation` | ERROR | A Glance `@Preview` or MultiPreview annotation on a function that is not `@Composable`. |

### `intellij-android-kotlin`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `IllegalIdentifier` | ERROR | A backticked identifier with characters that the dex format does not allow in a simple name, for example a space or a dot. These names fail on Android versions before 11. Files in host test source sets are skipped: a source set under `src/` whose name has the word `test`, for example `test`, `desktopTest` or `screenshotTest`, but not a device test set (`androidTest`, `androidInstrumentedTest`, `androidDeviceTest`). |

### `intellij-android-wear-tile-preview`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `WearTilePreviewComposableAnnotationIsNotSupported` | ERROR | A `@Composable` annotation on a Tile preview function. |
| `WearTilePreviewContextComesFromParameter` | ERROR | A `Context` in a Tile preview function that does not come from the `Context` parameter of the function. |
| `WearTilePreviewHasValidMethodSignature` | ERROR | A function with a Tile `@Preview` annotation that does not return `TilePreviewData`, or has parameters other than one `Context`. |
| `WearTilePreviewMethodIsAnnotatedWithTilePreviewAnnotation` | ERROR | A `Preview` annotation from another package, or a MultiPreview annotation that refers to one, on a function with the Tile preview signature. |
| `WearTilePreviewShouldBeTopLevelFunction` | ERROR | A function with a Tile `@Preview` annotation in another function, in a nested class, or in a class without a default constructor. |

### `intellij-compose-multiplatform-preview`

These rules check the Jetpack (`androidx.compose.ui.tooling.preview`) and the Compose Multiplatform
(`org.jetbrains.compose.ui.tooling.preview`) `@Preview` and `@PreviewParameter` annotations. A
MultiPreview annotation counts only on a `@Composable` function. If the same rule of
`intellij-android-compose-preview` (the rule name without `Multiplatform`) is active, these rules
do not report the problems that the Android rule reports for the Jetpack previews. Thus one problem
gives one finding. IntelliJ also shows only one of the two inspections in a file.

| Rule | IntelliJ level | Finds |
|---|---|---|
| `MultiplatformPreviewAnnotationInFunctionWithParameters` | ERROR | A preview function that has a parameter with no default value and no `@PreviewParameter`. |
| `MultiplatformPreviewMultipleParameterProviders` | ERROR | A preview function that has more than one `@PreviewParameter`. |

### `intellij-gradle-best-practices`

These rules check only Gradle Kotlin DSL scripts (`*.gradle.kts`). detekt checks a script only if
it is in the `source` of the detekt task, for example
`source.from(layout.projectDirectory.file("build.gradle.kts"))`. The rules use only the syntax of
the script, because detekt does not have the Gradle model.

| Rule | IntelliJ level | Finds |
|---|---|---|
| `AvoidApplyPluginMethod` | WEAK WARNING | An `apply(plugin = ...)` or `apply<T>()` call in place of the `plugins` block. |
| `AvoidDependencyNamedArgumentsNotation` | WEAK WARNING | A dependency in a `dependencies` block with a named `group` argument or with positional string coordinates, in place of one `group:name:version` string. |
| `AvoidDuplicateDependencies` | WEAK WARNING | A dependency that the top-level `dependencies` blocks declare more than one time in the same configuration. It does not check the configurations that extend other configurations. |
| `AvoidDuplicateRepositories` | WEAK WARNING | A repository that one `repositories` block declares more than one time. Whitespace and comments do not count. |
| `AvoidRepositoriesInBuildGradle` | WEAK WARNING | A `repositories` block in a `build.gradle.kts` script, except in `publishing`. Inactive by default. |
| `RedundantKotlinStdLibDependency` | WARNING | A `kotlin-stdlib` dependency with the version of the `kotlin("jvm")` plugin, which adds this dependency itself. It does not read `kotlin.stdlib.default.dependency` from `gradle.properties`. |
| `TaskMissingDescription` | WEAK WARNING | A task that `tasks.register`, `tasks.create`, `tasks.registering` or `tasks.creating` makes without a `description`. |

### `intellij-jvm-code-maturity`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `MarkedForRemoval` | ERROR | A usage of an API that is deprecated with `@Deprecated(forRemoval = true)`, or an override of a method marked for removal. | `isSameOutermostClassIgnored` (default `true`): do not report the usages in the same outermost class. |

### `intellij-jvm-declaration-redundancy`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `SameParameterValue` | WARNING | A parameter of a function that gets the same constant value in every call, when all calls are in the same file. | `highestModifier` (default `protected`): the highest visibility to check.<br>`minimalUsageCount` (default `1`): the number of calls needed to report. |
| `SameReturnValue` | WARNING | A function that cannot be overridden and that always returns the same constant value. | |

### `intellij-jvm-general`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `BlockingMethodInNonBlockingContext` | WARNING | A possibly blocking call, such as `Thread.sleep()`, a JDK method that throws `IOException` or a method annotated with `@Blocking`, in a non-blocking context such as a suspend function, a coroutine on a non-IO dispatcher or a method annotated with `@NonBlocking`. | `isUnknownContextBlocking` (default `true`): do not report the calls in contexts that are not known.<br>`isSuspendContextNonBlocking` (default `true`): consider suspend functions and suspend lambdas non-blocking.<br>`blockingAnnotations` (default the `Blocking` annotations of JetBrains, Micronaut and SmallRye): more annotations that mark blocking methods. The rule always uses the default ones.<br>`nonBlockingAnnotations` (default the `NonBlocking` annotations of JetBrains, Micronaut and SmallRye): more annotations that mark non-blocking contexts. The rule always uses the default ones.<br>`blockingLambdaFunctions` (default `runBlocking` and the suspend stubbing and verification functions of mockito-kotlin, MockK and Mokkery): functions whose lambda arguments run in a blocking context, given as fully qualified names. |
| `MissingDeprecatedAnnotationOnScheduledForRemovalApi` | ERROR | A declaration marked with `@ApiStatus.ScheduledForRemoval` that is not also marked with `@Deprecated`. | |
| `MustAlreadyBeRemovedApi` | ERROR | A declaration marked with `@ApiStatus.ScheduledForRemoval` for a version that is not later than the current version. | `currentVersion` (default empty, which turns the rule off): the current version. |
| `NonExtendableApiUsage` | WARNING | A class, interface or override that extends, implements or overrides a library API marked with `@ApiStatus.NonExtendable`, or the annotation on a declaration that cannot be extended. | |
| `OverrideOnly` | WARNING | A call or reference of a library method marked with `@ApiStatus.OverrideOnly` outside a super or delegate call in its override, or the annotation on a declaration that cannot be overridden. | |
| `SerializableHasSerialVersionUIDField` | WARNING | A `Serializable` class without a `serialVersionUID` field. Inactive by default. | `isAnonymousClassIgnored` (default `false`): do not report anonymous objects.<br>`ignoredSuperClasses` (default `java.awt.Component`): do not report the subclasses of these classes that do not implement `Serializable` directly. |
| `SuppressionAnnotation` | WARNING | A `@Suppress` or `@SuppressWarnings` annotation or a `//noinspection` comment that suppresses inspections. Inactive by default. | `allowedSuppressions` (default empty): the suppressed ids to accept. |
| `SystemGetProperty` | WARNING | A `System.getProperty()` call for `file.separator`, `path.separator`, `line.separator` or `file.encoding`, which a dedicated API gives directly. | |
| `ThreadRun` | WARNING | A call of `run()` on a `Thread`, which runs the code in the current thread instead of starting a new one. | |
| `UnstableApiUsage` | WARNING | A usage of a library API that an unstable API annotation such as `@ApiStatus.Experimental` or `@ApiStatus.Internal` marks, or whose signature refers to such a type. | `unstableApiAnnotations` (default the unstable API annotations of JetBrains, Guava, RxJava, Apache HttpClient and Gradle): the annotations that mark unstable APIs.<br>`isInsideImportsIgnored` (default `true`): do not report imports.<br>`isApiDeclaredInThisProjectIgnored` (default `true`): report only compiled library APIs. |
| `UnstableTypeUsedInSignature` | WARNING | A public declaration whose signature refers to a type marked with an unstable API annotation, but that does not have the same annotation itself. Inactive by default. | `unstableApiAnnotations` (default as in `UnstableApiUsage`): the annotations that mark unstable APIs. |
| `UrlHashCode` | WARNING | An `equals()` or `hashCode()` call or an `==` comparison on `java.net.URL`, or a map or set of URLs, which can resolve the host over the network. | |

### `intellij-jvm-junit`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `AssertEqualsCalledOnArray` | WARNING | An `assertEquals()` call on two arrays, which compares them by identity and not by content. Inactive by default. | |
| `AssertEqualsMayBeAssertSame` | WARNING | An `assertEquals()` call on two objects of a final class that does not override `equals()`, where `assertSame()` fits. Inactive by default. | |
| `IgnoredJUnitTest` | WARNING | A test class or test method that `@Ignore` or `@Disabled` turns off. A typealias from a library or an `actual` typealias, such as `kotlin.test.Ignore`, does not count. Inactive by default. | `isReasonAccepted` (default `true`): do not report the ones that give a reason. |
| `JUnit3StyleTestMethodInJUnit4Class` | WARNING | A `test...` method without `@Test` in a JUnit 4 class, which JUnit 4 does not run. | |
| `JUnit5AssertionsConverter` | WARNING | A JUnit 3 or JUnit 4 assertion or assumption in a JUnit 5 test. | |
| `JUnitMalformedDeclaration` | WARNING | A JUnit test class, test method, lifecycle method, rule, extension or parameter source with a declaration that JUnit cannot run or rejects. | `ignorableAnnotations` (default `mockit.Mocked`, `org.junit.jupiter.api.io.TempDir`): the parameter annotations to accept.<br>`areSuspendFunctionsAllowed` (default `false`): accept suspend test and lifecycle functions. |
| `JUnitMixedFramework` | WARNING | A test class that uses annotations of a JUnit version other than the version of the class. | |
| `Junit4RunWithInspection` | WARNING | A `@RunWith` on a test class whose superclass already has `@RunWith`. | |
| `MigrateAssertToMatcherAssert` | WARNING | A JUnit assertion such as `assertEquals()` that a Hamcrest `assertThat()` can replace, when Hamcrest is on the classpath. Inactive by default. | |
| `ParameterizedParametersStaticCollection` | WARNING | A JUnit 4 `Parameterized` test class with no `@Parameters` method, more than one, or one with a bad signature. Inactive by default. | |
| `SuperTearDownInFinally` | WARNING | A `super.tearDown()` call in a JUnit 3 `tearDown()` that is not in a `finally` block. Inactive by default. | |
| `UseOfObsoleteAssert` | WARNING | A call of the JUnit 3 `junit.framework.Assert` or `TestCase` assertions, when JUnit 4 or later is on the classpath. Inactive by default. | |

### `intellij-jvm-logging`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `LogStatementNotGuardedByLogCondition` | INFORMATION | A logging call with computed arguments that a log level condition such as `if (logger.isDebugEnabled)` does not guard. Inactive by default. | `limitLevelType` (default `DEBUG_AND_LOWER`): the log levels to check, `ALL`, `WARN_AND_LOWER`, `INFO_AND_LOWER`, `DEBUG_AND_LOWER` or `TRACE`.<br>`isUnguardedConstantFlagged` (default `false`): also report the calls with only constant arguments. |
| `LoggingConditionDisagreesWithLogLevelStatement` | WARNING | A log level condition whose level does not agree with the level of the logging call that it guards. Inactive by default. | |
| `LoggingPlaceholderCountMatchesArgumentCount` | WARNING | A logging call whose number of placeholders does not match the number of arguments. | `slf4jToLog4J2Type` (default `AUTO`): tells if SLF4J calls go to Log4j 2, `AUTO`, `YES` or `NO`. |
| `LoggingStringTemplateAsArgument` | WARNING | A logging call that builds its message with a string template or concatenation instead of placeholders. | `limitLevelType` (default `DEBUG_AND_LOWER`): the log levels to check, `ALL`, `WARN_AND_LOWER`, `INFO_AND_LOWER`, `DEBUG_AND_LOWER` or `TRACE`.<br>`arePrimitivesSkipped` (default `true`): do not report templates with only primitive values.<br>`isOnlyExceptionSkipped` (default `false`): do not report the calls whose only other argument is an exception. |

### `intellij-jvm-test-frameworks`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `AssertBetweenInconvertibleTypes` | WARNING | A JUnit, TestNG or AssertJ equality or identity assertion between objects of inconvertible types. | |
| `TestCaseWithConstructor` | WARNING | A test class that does initialization work in a constructor or an `init` block instead of a set-up method. IntelliJ has the same check for initializers, but turns it off until its UAST highlighting supports them. The rule keeps it, because an `init` block holds the constructor logic of a Kotlin class. Inactive by default. | |
| `TestCaseWithNoTestMethods` | WARNING | A test class without test methods. Inactive by default. | `isSupersIgnored` (default `true`): do not report a class when a superclass has test methods. |
| `TestMethodWithoutAssertion` | WARNING | A test method without assertions. Inactive by default. | `assertionMethods` (default the assertions of JUnit, kotlin.test, TestNG, AssertJ, Truth, Hamcrest, Mockito, MockK, JMockit, assertk, Mokkery, mockito-kotlin, Compose UI test, Turbine, Koin and Kotest): the assertion methods as `<class or package>#<method name regex>`.<br>`isAssertKeywordAssertion` (default `true`): count `assert()` as an assertion.<br>`isThrowingIgnored` (default `false`): do not report a method with `@Throws`. |

### `intellij-kotlin-code-migration`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `CanConvertToMultiDollarString` | WEAK WARNING | A string that escapes two or more dollar characters (`\$`), or writes one as `${'$'}`, and can use an interpolation prefix instead. Each escaped dollar is reported. Unlike IntelliJ, a string whose only escape is a single `\$`, such as `"%1\$s"`, is not reported. Opposite of `ConvertFromMultiDollarToRegularString`. |
| `ConvertFromMultiDollarToRegularString` | INFORMATION | A string with an interpolation prefix that can be a regular string. Opposite of `CanConvertToMultiDollarString`, which is active by default. Inactive by default. |
| `ConvertLongToDuration` | WARNING | A kotlinx.coroutines call that uses the `Long` milliseconds overload instead of `Duration`. |
| `InfixCallToOrdinary` | INFORMATION | An infix call that can be an ordinary call. Opposite of `ConvertPairConstructorToToFunction` and `ReplaceToWithInfixForm`, of which `ReplaceToWithInfixForm` is active by default. Inactive by default. |

### `intellij-kotlin-coroutines`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `CoroutineContextWithJob` | WARNING | A `Job` or `NonCancellable` in the context argument of a coroutine builder, which breaks structured concurrency. |
| `DeferredResultUnused` | WARNING | A call that returns a `Deferred` whose result is never used. Unlike IntelliJ IDEA, a call whose declared return type is a type parameter, such as `map.remove(key)` on a map of `Deferred` values, is skipped. |
| `ForEachJoinOnCollectionOfJob` | WARNING | A `forEach { it.join() }` on a collection of jobs that `joinAll()` can replace. |
| `MapAwaitOnCollectionOfDeferred` | WARNING | A `map { it.await() }` on a collection of deferred values that `awaitAll()` can replace. |
| `PreferCurrentCoroutineContextToCoroutineContext` | WARNING | A use of `kotlin.coroutines.coroutineContext` that `currentCoroutineContext()` should replace. |
| `RunBlockingInSuspendFunction` | WARNING | A `runBlocking` call inside a suspend function or a suspend lambda. |
| `SimplifiableFlowCall` | WEAK WARNING | A `Flow` call with a trivial lambda that `flattenMerge`, `flattenConcat`, `filterNotNull` or `filterIsInstance` can replace. |
| `SimplifiableFlowCallChain` | WEAK WARNING | A chain of two `Flow` calls that one call can replace, such as `filter {}.first()`. |
| `SuspendCoroutineLacksCancellationGuarantees` | WARNING | A `suspendCoroutine` call that ignores cancellation when `suspendCancellableCoroutine` is available. |
| `SuspiciousImplicitCoroutineScopeReceiverAccess` | WARNING | An implicit access to an outer `CoroutineScope` receiver from inside a suspending lambda or function. |
| `SuspiciousMutableCollectionInStateFlow` | WARNING | A `MutableStateFlow` that holds a mutable collection, which emits no new value when it changes in place. |
| `UnusedFlow` | WARNING | A `Flow` that is created but never collected, returned or passed on, except inside MockK verification blocks such as `verify {}`. Overlaps detekt's `IgnoredReturnValue`, which is active by default. |
| `UselessCallOnFlow` | WARNING | A `filterNotNull`, `filterIsInstance` or `mapNotNull` call on a `Flow` that does nothing or can be simpler. |

### `intellij-kotlin-java-interop`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `JavaCollectionWithNullableTypeArgument` | WARNING | A Java concurrent collection or `PriorityQueue` with a nullable type argument, which does not support `null`. |
| `JavaCollectionsStaticMethodOnImmutableList` | WARNING | A `java.util.Collections` mutator call (`reverse`, `sort`, `shuffle`, `fill`) on a read-only Kotlin list. |
| `JavaDefaultMethodsNotOverriddenByDelegation` | WARNING | Interface delegation that does not forward the overrides of Java default methods of the delegate. By default, common source sets (`commonMain`, `commonTest`) are excluded (`excludes`), because their interfaces have no Java default methods. |

### `intellij-kotlin-logging`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `KotlinLoggerInitializedWithForeignClass` | WARNING | A logger that is created with the class literal of another class. Inactive by default. |

### `intellij-kotlin-naming-conventions`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `InconsistentCommentForJavaParameter` | WARNING | A `/* name = */` comment before an argument of a Java call that does not match the parameter name. |
| `LocalVariableName` | WEAK WARNING | A local variable, destructuring entry or non-property parameter whose name is not lowerCamelCase with only letters and digits. |

### `intellij-kotlin-numeric-issues`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `KotlinBigDecimalEquals` | WARNING | An `equals()` or `==` comparison of `BigDecimal` values that should use `compareTo()`. Inactive by default. |

### `intellij-kotlin-other-problems`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `ConvertArgumentToSet` | WEAK WARNING | A collection argument of `minus`, `intersect`, `subtract`, `removeAll` or `retainAll` that should be a `Set`. | |
| `DeprecatedCallableAddReplaceWith` | WEAK WARNING | A `@Deprecated` callable with a simple body but no `replaceWith` argument. | `areOverridesSkipped` (default `false`): do not report `override` functions and properties. |
| `EnumValuesSoftDeprecate` | WARNING | A `values()` call of an enum class that `entries` can replace. | |
| `EnumValuesTopLevelFunctionSoftDeprecate` | WARNING | An `enumValues<T>()` call that `enumEntries<T>()` can replace. | |
| `FloatingPointLiteralPrecision` | WEAK WARNING | A floating-point literal with more digits than its type can hold. | |
| `MigrateDiagnosticSuppression` | WARNING | An old diagnostic name in `@Suppress` that has a new name. | |
| `ReplaceWithEnumMap` | WARNING | A `HashMap` with enum keys that an `EnumMap` can replace. | |
| `ReplaceWithStringBuilderAppendRange` | WARNING | An `append(CharArray, Int, Int)` call that `appendRange` can replace. | |

### `intellij-kotlin-probable-bugs`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `AmbiguousNonLocalJump` | WEAK WARNING | An unlabeled `break` or `continue` in an inline lambda inside a loop. | |
| `ArrayInDataClass` | WARNING | An array property in a data or value class that does not override `equals()` and `hashCode()`. | |
| `AssignedValueIsNeverRead` | WARNING | An assignment to a local variable whose value is never read. | |
| `CanSealedSubClassBeObject` | WEAK WARNING | A subclass of a sealed class that has no state and no `equals()` and can be an object. | |
| `ConflictingExtensionProperty` | WARNING | An extension property that a synthetic Java property of the receiver shadows. | |
| `ConvertNaNEquality` | WARNING | An equality check with `Double.NaN` or `Float.NaN` that should use `isNaN()`. | |
| `DataClassPrivateConstructor` | WARNING | A private primary constructor of a data class that the generated `copy()` exposes. | |
| `DelegationToVarProperty` | WARNING | Class delegation to a `var` constructor property that the class never reassigns. | |
| `DuplicateArgumentsInSetOfAndMapOfFunctions` | WARNING | A duplicate constant element in a `setOf` call or a duplicate constant key in a `mapOf` call. | |
| `EmptyRange` | WARNING | A range whose constant start is past its end in the direction of the operator. | |
| `FilterIsInstanceResultIsAlwaysEmpty` | WARNING | A `filterIsInstance` call whose target type no element of the receiver can have. | |
| `ForEachParameterNotUsed` | WEAK WARNING | A `forEach` call whose lambda never uses the implicit `it` parameter. | |
| `ImplicitNullableNothingType` | WEAK WARNING | A `var` or open declaration without an explicit type whose inferred type is `Nothing?`. | |
| `IncompleteDestructuring` | INFORMATION | A destructuring declaration of a data class that omits some of its components. Inactive by default. | |
| `JavaIoSerializableObjectMustHaveReadResolve` | WARNING | A `java.io.Serializable` object without `readResolve`. | |
| `KotlinArrayHashCode` | WARNING | A `hashCode()` call on an array that should be `contentHashCode()`. | |
| `KotlinArrayToString` | WARNING | An explicit or implicit `toString()` call on an array that should be `contentToString()`. | |
| `KotlinEqualsBetweenInconvertibleTypes` | WARNING | An `equals()` call between primitives, strings or enums of different types, which always returns false. | |
| `KotlinMisorderedAssertEqualsArguments` | WARNING | An `assertEquals()`-like call that passes the constant as the actual value and the tested value as the expected one. | |
| `KotlinThrowableNotThrown` | WARNING | A `Throwable` that a call creates or returns and that is never thrown, returned or used. By default, test source sets are excluded (`excludes`). | |
| `LateinitVarOverridesLateinitVar` | WARNING | A `lateinit var` that overrides a `lateinit var`. | |
| `LazyWithoutDelegation` | WEAK WARNING | A private or local property that holds a `Lazy` and reads it only through `value`, where `by` delegation fits. | |
| `MainFunctionReturnUnit` | WARNING | A `main` function that would be an entry point but does not return `Unit`. | |
| `RecursiveEqualsCall` | WARNING | An `equals` implementation that compares `this` with its parameter through `==`, `!=` or `equals`. | |
| `RecursivePropertyAccessor` | WARNING | A property accessor or a synthetic Java accessor override that accesses its own property. | |
| `RedundantLabel` | WARNING | A label on an expression that no `break`, `continue` or `return` can reference. | |
| `SelfAssignment` | WARNING | An assignment of a variable or property to itself. | |
| `SelfReferenceConstructorParameter` | WARNING | A primary constructor with a non-null parameter of its own class type, which no code can call. | |
| `SetterBackingFieldAssignment` | WARNING | A setter that never assigns the backing field, so the new value is lost. | |
| `SuspiciousCallOnCollectionToAddOrRemovePath` | WARNING | A `plus` or `minus` call that iterates over a self-iterable argument such as a `Path` instead of using it as one element. | |
| `SuspiciousCascadingIf` | WARNING | An operator or call after the last `else` block of a cascading `if` that applies only to the nested `if`. | |
| `SuspiciousCollectionReassignment` | WEAK WARNING | A `+=` or `-=` on a `var` of a read-only collection that creates a new collection each time. | |
| `SuspiciousEqualsCombination` | WEAK WARNING | A condition that compares the same variable with both `==` and `===`. | |
| `SuspiciousGetterForMutableObject` | WARNING | A getter that creates a new mutable collection or coroutine object on each access. | |
| `SuspiciousJavaClassCallableReference` | WARNING | A `::javaClass` callable reference that was meant to be `.javaClass` or `::class.java`. | |
| `SuspiciousVarProperty` | WARNING | A `var` property whose getter never reads the backing field, so the setter has no visible effect. | |
| `UnusedDataClassCopyResult` | WARNING | A data class `copy` call whose result is not used. | |
| `UnusedEquals` | WARNING | An `==` expression or `equals` call whose result is not used. | |
| `UnusedExpression` | WARNING | An expression whose value is not used and that has no effect. | |
| `UnusedLambdaExpression` | WARNING | A lambda expression that is a statement and is never called. | |
| `UnusedLambdaExpressionBody` | WARNING | An unused call of a function whose expression body is a lambda, so the lambda never runs. | |
| `UselessCallOnCollection` | WARNING | A `filterNotNull`, `filterIsInstance`, constant `filter` or `mapNotNull`-like call on a collection or sequence that does nothing or can be simpler. | |
| `VariableNeverRead` | WARNING | A local variable that gets values but is never read. | |
| `WrapUnaryOperator` | WEAK WARNING | A unary minus or plus before a number literal with a call, where the operator applies to the call result. | `ignoredSelectors` (default empty): do not report these properties and functions after the literal, given as fully qualified names, e.g. `androidx.compose.ui.unit.dp`. |

### `intellij-kotlin-redundant-constructs`

| Rule | IntelliJ level | Finds |
|---|---|---|
| `CanBeParameter` | WARNING | A `val` or `var` constructor parameter that is only used during initialization. Only parameters that are not visible outside the file are checked. |
| `CanBePrimaryConstructorProperty` | WARNING | A property that is initialized from the constructor parameter of the same name and type. |
| `CanUnescapeDollarLiteral` | WEAK WARNING | An escaped dollar in a string literal that can be a plain `$`. |
| `ConstantConditionIf` | WEAK WARNING | An `if` whose condition is the constant `true` or `false`. |
| `ExplicitThis` | INFORMATION | An explicit `this` receiver that the code resolves the same without. Opposite of `ImplicitThis`. Inactive by default. |
| `IfExpressionWithIdenticalBranches` | WEAK WARNING | An `if` whose `then` and `else` branches are identical. |
| `KotlinRedundantOverride` | WEAK WARNING | An override that only calls the super implementation with the same arguments. |
| `NullChecksToSafeCall` | WEAK WARNING | Chained null checks on a receiver and on a call on it that one safe call check can replace. |
| `RedundantCompanionReference` | WARNING | An explicit companion object reference that the code resolves the same without. |
| `RedundantElvisReturnNull` | WARNING | A `?: return null` in a returned expression that is null anyway. |
| `RedundantEnumConstructorInvocation` | WEAK WARNING | Empty parentheses after an enum entry. |
| `RedundantGetter` | WEAK WARNING | A getter that only returns the backing field. |
| `RedundantIf` | WEAK WARNING | An `if` that only chooses between `true` and `false`, or a constant and a boolean expression. |
| `RedundantInterpolationPrefix` | WEAK WARNING | A multi-dollar interpolation prefix on a string that does not need it. |
| `RedundantLabeledReturnOnLastExpressionInLambda` | INFORMATION | A `return@label` on the last expression of the lambda with that label. |
| `RedundantLambdaArrow` | WARNING | A lambda arrow with no parameters or with only an explicit `it` parameter. |
| `RedundantLambdaOrAnonymousFunction` | WARNING | A lambda or an anonymous function that is called at once. |
| `RedundantModalityModifier` | WARNING | A modality modifier that equals the default modality of the declaration. Skips `abstract` in an interface, which detekt `OptionalAbstractKeyword` reports. |
| `RedundantNullableReturnType` | WARNING | A nullable return type of a function or a read-only property that never returns null. |
| `RedundantReturnKeyword` | WARNING | A `return` in a branch of an `if`, `when` or elvis expression that is already returned. |
| `RedundantReturnLabel` | WEAK WARNING | A label on a `return` that targets the enclosing function, not a lambda. |
| `RedundantSamConstructor` | WARNING | A SAM constructor call in an argument where a plain lambda converts to the same type. |
| `RedundantSetter` | WEAK WARNING | A setter that is the default one or only assigns the value to the backing field. |
| `RedundantUpperBound` | WARNING | A type parameter bound that is `Any?`, the default upper bound. |
| `RedundantValueArgument` | WEAK WARNING | An argument whose constant value equals the default value of the parameter. Skips generated functions, such as the data class `copy`. Inactive by default. |
| `RedundantWith` | WEAK WARNING | A `with` call whose lambda does not use the receiver. |
| `RemoveExplicitSuperQualifier` | WEAK WARNING | A `super<T>` qualifier that does not change the member that the call resolves to. |
| `RemoveExplicitTypeArguments` | WEAK WARNING | Explicit type arguments that the compiler can infer. |
| `RemoveRedundantQualifierName` | WARNING | A package, class or companion qualifier that the code resolves the same without. Keeps a `kotlin.jvm` or `java.lang` qualifier in a common source set. |
| `RemoveRedundantSpreadOperator` | WARNING | A spread operator on an array that is created in place with an `arrayOf`-like call or a `[...]` literal. |
| `RemoveSetterParameterType` | WARNING | An explicit type on a setter parameter. |
| `RemoveSingleExpressionStringTemplate` | WARNING | A string template that only holds one non-null `String` expression. |
| `SimplifyWhenWithBooleanConstantCondition` | WEAK WARNING | A subjectless `when` with a `true` or `false` branch condition. |
| `SuspiciousCallableReferenceInLambda` | WEAK WARNING | A lambda whose only statement is a callable reference. |
| `UnnecessaryOptInAnnotation` | WEAK WARNING | An `@OptIn` annotation or marker that no used API requires. |
| `UnnecessaryVariable` | WEAK WARNING | A local variable that only copies another local value or parameter. |
| `UnusedContextParameterCall` | WEAK WARNING | A `context(...)` call whose block does not use all the arguments, or that passes a context parameter that is already in scope. |
| `UnusedReceiverParameter` | WARNING | An extension receiver that is never used. Skips a body with a call that does not resolve. |
| `VariableInitializerIsRedundant` | WARNING | A variable initializer that is overwritten before it is read. |
| `WhenWithOnlyElse` | WEAK WARNING | A `when` with only an `else` branch. Inactive by default. |

### `intellij-kotlin-style-issues`

| Rule | IntelliJ level | Finds | Options |
|---|---|---|---|
| `AddOperatorModifier` | INFORMATION | A function that matches an operator convention but has no `operator` modifier. A `set` function that does not return `Unit` is not reported. Inactive by default. | `areSuspendFunctionsSkipped` (default `true`): do not report `suspend` functions. |
| `AddVarianceModifier` | INFORMATION | A class type parameter that is used only in input or only in output positions and can be `in` or `out`. Inactive by default. | |
| `BooleanLiteralArgument` | WEAK WARNING | Adjacent `true` or `false` arguments without parameter names. | `ignoredFunctions` (default empty): do not report the arguments of these functions, given as fully qualified names, e.g. `androidx.compose.ui.graphics.vector.PathBuilder.arcTo`. |
| `CascadeIf` | WEAK WARNING | An `if`-`else if` chain on one subject that `when` can replace. | |
| `CollectionConcatenationToBuildCollection` | WEAK WARNING | A chain of `+` or `-` operations on a list or set that `buildList` or `buildSet` can replace. Inactive by default. | `minimalOperationCount` (default `2`): the number of operations in a chain needed to report. |
| `ConvertPairConstructorToToFunction` | INFORMATION | An explicit `Pair(a, b)` constructor call without named arguments that can be the infix `a to b`. Opposite of `InfixCallToOrdinary`. Inactive by default. | |
| `ConvertRangeCheckToTwoComparisons` | INFORMATION | An `in` or `!in` check of simple values against a standard range that can be two comparisons. Opposite of `ConvertTwoComparisonsToRangeCheck`, which is active by default. Inactive by default. | |
| `ConvertReferenceToLambda` | INFORMATION | A callable reference that a lambda can replace. Inactive by default. | |
| `ConvertSealedClassToSealedInterface` | INFORMATION | A sealed class without state, constructor parameters, final, `internal` or `protected` members that can be a sealed interface. Members that an interface cannot have, such as non-private `inline` or `tailrec` functions, `external` members and `@JvmOverloads`, `@JvmName` or `@Synchronized` members, block the conversion. Opposite of `ConvertSealedInterfaceToSealedClass`. Overlaps detekt's `AbstractClassCanBeInterface` when its `ignoreSealedClasses` is `false`. | |
| `ConvertSealedInterfaceToSealedClass` | INFORMATION | A sealed interface whose inheritors are all plain classes or objects that can extend a sealed class. Opposite of `ConvertSealedClassToSealedInterface`, which is active by default. Inactive by default. | |
| `ConvertSecondaryConstructorToPrimary` | INFORMATION | A secondary constructor that every other constructor delegates to and that can be the primary constructor. | |
| `ConvertToExplicitBackingFields` | WEAK WARNING | A property whose getter only returns a private property of a narrower type, which an explicit backing field can replace. Only when the language version and the Kotlin standard library on the classpath are 2.4 or later, or when the compiler options enable explicit backing fields. Opposite of `RevertExplicitBackingFields`. | |
| `ConvertToStringTemplate` | WEAK WARNING | A `String` concatenation of literals and simple values that a string template can replace. | |
| `ConvertTryFinallyToUseCall` | WEAK WARNING | A `try`-`finally` that only closes a `Closeable` in `finally`, which `use()` can replace. | |
| `ConvertTwoComparisonsToRangeCheck` | WEAK WARNING | Two comparisons of one value with a lower and an upper bound that an `in` or `!in` range check can replace. Opposite of `ConvertRangeCheckToTwoComparisons`. | |
| `CopyWithoutNamedArguments` | WEAK WARNING | A data class `copy` call that passes arguments without names. | |
| `DestructuringDeclaration` | WEAK WARNING | A loop variable of a data class or `Map.Entry` type that is only used to read its components. | `areNonParameterCasesReported` (default `false`): also report local variables and lambda parameters.<br>`maxDestructuringEntries` (default `3`, as in detekt's `DestructuringDeclarationWithTooManyEntries`): the maximum number of entries of the suggested declaration. |
| `FilterIsInstanceCallWithClassLiteralArgument` | WEAK WARNING | A `filterIsInstance(X::class.java)` call that `filterIsInstance<X>()` can replace. | |
| `FoldInitializerAndIfToElvis` | WEAK WARNING | An `if` null check that exits right after a variable declaration and can fold into the initializer with `?:`. | |
| `IfThenToElvis` | WEAK WARNING | An `if` that checks a value for `null` or a type and that the elvis operator `?:` can replace. | |
| `IfThenToSafeAccess` | WEAK WARNING | An `if` that checks a value for `null` or a type, returns `null` otherwise, and that `?.` or `as?` can replace. | |
| `ImplicitThis` | INFORMATION | A member access or callable reference that uses an implicit `this` receiver. Opposite of `ExplicitThis`, which is active by default. Inactive by default. | |
| `IntroduceWhenSubject` | WEAK WARNING | A `when` without a subject whose branches all check the same value, which can be the subject. | `areWhenGuardsIgnored` (default `true`): do not report a `when` with a branch condition joined by `&&`, which becomes a guard. |
| `JavaMapForEach` | WEAK WARNING | A call of the Java `Map.forEach` with a two-parameter lambda instead of the Kotlin `forEach` with a destructured entry. | |
| `JoinDeclarationAndAssignment` | WEAK WARNING | A property without an initializer that can join its first assignment. | |
| `LiftReturnOrAssignment` | WEAK WARNING | An `if`, `when` or `try` whose branches all end in a return or in an assignment to the same variable. | |
| `MapToForEach` | WEAK WARNING | A `map`-like call whose result is not used and that `forEach` or `forEachIndexed` can replace. | |
| `MoveLambdaOutsideParentheses` | WEAK WARNING | A lambda passed as the last argument inside the parentheses that can be a trailing lambda. | |
| `MoveVariableDeclarationIntoWhen` | WEAK WARNING | A variable declared right before a `when` and used only in it, which can move into the `when` subject. | |
| `NullableHashCode` | WEAK WARNING | An `x?.hashCode() ?: 0` on a nullable receiver that `x.hashCode()` can replace. | |
| `RedundantAsSequence` | WEAK WARNING | An `asSequence()` call on a sequence, or on an iterable followed by one terminal operation. | |
| `RedundantElseInIf` | INFORMATION | An `else` branch after `if` branches that all end with a jump such as `return` or `throw`. | |
| `RedundantObjectTypeCheck` | INFORMATION | An `is` check against a non-data object type that should be `===` or `!==`. Inactive by default. | |
| `RedundantRunCatching` | WEAK WARNING | A `runCatching { }.getOrThrow()` chain that `run { }` can replace. | |
| `RemoveEmptyParenthesesFromAnnotationEntry` | WEAK WARNING | Empty parentheses after an annotation that needs no arguments. | |
| `ReplaceAddAllWithMapTo` | INFORMATION | An `addAll` or `+=` of a `map` or `filter` result that `mapTo` or `filterTo` can replace. | |
| `ReplaceAssertBooleanWithAssertEquality` | WEAK WARNING | An `assertTrue` or `assertFalse` call with an `==` or `===` check that an equality assertion can replace. Unlike IntelliJ IDEA, a check against a `true`, `false` or `null` literal is skipped. | |
| `ReplaceAssociateFunction` | WEAK WARNING | An `associate` or `associateTo` call that `associateBy` or `associateWith` can replace. Inactive by default. | |
| `ReplaceCallWithBinaryOperator` | WEAK WARNING | An explicit call of `equals` or `compareTo` that a binary operator can replace. | |
| `ReplaceCollectionCountWithSize` | WEAK WARNING | A `count()` call without a predicate on a collection, array or map that `size` can replace. Inactive by default. | |
| `ReplaceContains` | INFORMATION | A call of an operator `contains` function that the `in` operator can replace. | |
| `ReplaceIfExpressionWithFirstOrNull` | WEAK WARNING | An `if` that returns the first element of a non-empty collection, string or array, else `null`, which `firstOrNull()` can replace. | |
| `ReplaceJavaStaticMethodWithKotlinAnalog` | WEAK WARNING | A call of a Java static method that has a Kotlin standard library counterpart. | |
| `ReplaceManualRangeWithIndicesCalls` | WEAK WARNING | A manual range from `0` to the size or last index that `indices`, a loop over the elements or `withIndex()` can replace. | |
| `ReplaceMapGetOrDefault` | INFORMATION | A `map.getOrDefault(key, default)` call on a map with non-null values that `map[key] ?: default` can replace. | |
| `ReplaceMapIndexedWithListGenerator` | WEAK WARNING | A `mapIndexed` call on a collection that ignores the element, where `List(size) { index -> ... }` fits. | |
| `ReplaceMapKeysCallChainWithKeys` | INFORMATION | A `map { it.key }.toSet()` call chain on a map that `keys` can replace. | |
| `ReplaceNegatedIsEmptyWithIsNotEmpty` | WEAK WARNING | A negated `isEmpty`, `isNotEmpty`, `isBlank` or `isNotBlank` call that the inverted function can replace. | |
| `ReplaceNotNullAssertionWithElvisReturn` | WEAK WARNING | A `!!` in a function or labeled lambda that returns `Unit` or a nullable type, where `?: return` fits. Inactive by default. By default, test source sets are excluded (`excludes`), because there `!!` is an assertion. | |
| `ReplaceRangeStartEndInclusiveWithFirstLast` | WEAK WARNING | A `start` or `endInclusive` on a primitive range that the unboxed `first` or `last` can replace. | |
| `ReplaceReadLineWithReadln` | WEAK WARNING | A `readLine()` call that `readln()` or `readlnOrNull()` can replace. | |
| `ReplaceSizeCheckWithIsNotEmpty` | WEAK WARNING | A size, length or count comparison that `isNotEmpty()` can replace. | |
| `ReplaceSizeZeroCheckWithIsEmpty` | WEAK WARNING | A size, length or count zero check that `isEmpty()` can replace. | |
| `ReplaceStringFormatWithLiteral` | INFORMATION | A `String.format` call with only `%s` placeholders that a string template can replace. | |
| `ReplaceSubstringWithDropLast` | INFORMATION | An `s.substring(0, s.length - n)` call that `s.dropLast(n)` can replace. | |
| `ReplaceSubstringWithIndexingOperation` | INFORMATION | A `substring(i, i + 1)` call with constant indices that `s[i]` can replace. | |
| `ReplaceSubstringWithSubstringAfter` | INFORMATION | An `s.substring(s.indexOf(x))` call that `s.substringAfter(x)` can replace. | |
| `ReplaceSubstringWithSubstringBefore` | INFORMATION | An `s.substring(0, s.indexOf(x))` call that `s.substringBefore(x)` can replace. | |
| `ReplaceSubstringWithTake` | INFORMATION | An `s.substring(0, n)` call that `s.take(n)` can replace. | |
| `ReplaceToStringWithStringTemplate` | INFORMATION | A `toString()` call on a reference that a string template can replace. Inactive by default. | |
| `ReplaceToWithInfixForm` | WEAK WARNING | A dot call of the infix `to` function that can use the infix form. Opposite of `InfixCallToOrdinary`. | |
| `ReplaceUntilWithRangeUntil` | INFORMATION | An infix `until` call from the standard library that the `..<` operator can replace. | |
| `ReplaceWithCallWithContextCall` | WEAK WARNING | A `with` call whose receiver is only used as a context argument, so a `context` call fits. | |
| `ReplaceWithIgnoreCaseEquals` | WEAK WARNING | An equality check of two identical case conversions that `equals(..., ignoreCase = true)` can replace. | |
| `ReplaceWithImportAlias` | INFORMATION | A qualified name that an existing import alias can replace. | |
| `ReplaceWithOperatorAssignment` | WEAK WARNING | An assignment such as `x = x + y` that an operator assignment such as `x += y` can replace. | |
| `RevertExplicitBackingFields` | INFORMATION | A property with an explicit backing field that a private backing property can replace. Opposite of `ConvertToExplicitBackingFields`, which is active by default. Inactive by default. | |
| `SafeCastWithReturn` | WEAK WARNING | An `x as? T ?: return` statement that an `if (x !is T) return` check can replace. | |
| `SimplifiableCall` | WEAK WARNING | A `flatMap`, `filter` or `mapNotNull` call with a trivial lambda that `flatten`, `filterNotNull` or `filterIsInstance` can replace. | |
| `SimplifiableCallChain` | WEAK WARNING | A collection, sequence or text call chain that one call can replace. | |
| `SimplifyAssertNotNull` | INFORMATION | An `assert(x != null)` right after the declaration of `x` that can fold into the initializer. | |
| `SimplifyBooleanWithConstants` | WEAK WARNING | A boolean expression with a constant operand that can be simpler, such as `x && true`. Unlike IntelliJ IDEA, a constant that refers to a named value, such as a generated build config flag, is not folded. | |
| `SimplifyNegatedBinaryExpression` | WEAK WARNING | A negated comparison, `is` or `in` check, or boolean literal that the inverted operator can replace. | |
| `SimplifyNestedEachInScopeFunction` | WEAK WARNING | An `also` or `apply` whose only statement is `forEach` or `onEach` on the receiver, which `onEach` can replace. | |
| `UnclearPrecedenceOfBinaryExpression` | WARNING | An expression that mixes `?:` with equality, comparison, `in`, `is`, arithmetic or infix calls, or `||` in a `when` guard, without parentheses. | |
| `UnlabeledReturnInsideLambda` | INFORMATION | An unlabeled `return` inside an inline lambda that returns from the enclosing function. Inactive by default. | |
| `UnsafeCastWithReturn` | WARNING | An unsafe cast followed by `?: return` that should be the safe cast `as?`. | |
| `UseExpressionBody` | INFORMATION | A function or property accessor whose block body holds only a one-line `return`. | |
| `UsePropertyAccessSyntax` | WEAK WARNING | A call of a Java getter or setter that Kotlin property access syntax can replace. | |
| `VerboseNullabilityAndEmptiness` | WEAK WARNING | A null check followed by an emptiness or blankness check that `isNullOrEmpty()` or `isNullOrBlank()` can replace. | |

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
