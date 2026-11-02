package by.overpas.detekt.intellij.jvm.testframeworks.testmethodwithoutassertion

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

@IntellijInspection("TestMethodWithoutAssertion")
class TestMethodWithoutAssertion(config: Config) :
    Rule(
        config,
        "A test method does not contain assertions, so it does not verify the result. " +
            "Add an assertion to the test.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Methods that count as assertions, as `<class or package fq name>#<method name regex>`. " +
            "Use the package name for top-level functions.",
    )
    private val assertionMethods: List<TestMethodWithoutAssertionMethod> by config(DEFAULT_ASSERTION_METHODS) {
        it.mapNotNull(TestMethodWithoutAssertionMethod::parse)
    }

    @Configuration("Count a call to the Kotlin `assert()` function as an assertion.")
    private val isAssertKeywordAssertion: Boolean by config(true)

    @Configuration("Do not report a test method that declares exceptions with `@Throws`.")
    private val isThrowingIgnored: Boolean by config(false)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val owner = function.containingClassOrObject as? KtClass ?: return
        val matcher = TestMethodWithoutAssertionMatcher(assertionMethods, isAssertKeywordAssertion)
        val isReported = analyze(function) {
            function.isTestWithoutAssertion(owner, isThrowingIgnored) && !context(matcher) { function.hasAssertion() }
        }
        if (isReported) {
            report(
                Finding(Entity.atName(function), "Test method '${function.nameAsSafeName}()' contains no assertions"),
            )
        }
    }

    private companion object {
        val DEFAULT_ASSERTION_METHODS = listOf(
            "org.junit.Assert#assert.*|fail.*",
            "junit.framework.Assert#assert.*|fail.*",
            "org.junit.jupiter.api.Assertions#assert.*|fail.*",
            "org.junit.jupiter.api#assert.*|fail.*",
            "org.assertj.core.api.Assertions#assertThat.*",
            "org.assertj.core.api.WithAssertions#assertThat",
            "com.google.common.truth.Truth#assert.*",
            "com.google.common.truth.Truth8#assert.*",
            "org.mockito.Mockito#verify.*",
            "org.mockito.InOrder#verify",
            "org.junit.rules.ExpectedException#expect.*",
            "io.mockk#verify.*",
            "org.hamcrest.MatcherAssert#assertThat",
            "mockit.Verifications#Verifications",
            "kotlin.test#assert.*|fail.*|expect",
            "org.testng.Assert#assert.*|fail.*|expect.*",
            "org.testng.AssertJUnit#assert.*|fail.*",
            "assertk#assert.*|fail",
            "dev.mokkery#verify.*",
            "org.mockito.kotlin#verify.*",
            "androidx.compose.ui.test#assert.*",
            "androidx.compose.ui.test.SemanticsNodeInteraction#assert.*",
            "androidx.compose.ui.test.SemanticsNodeInteractionCollection#assert.*",
            "app.cash.turbine#test",
            "app.cash.turbine.ReceiveTurbine#await.*|expect.*|ensureAllEventsConsumed",
            "org.koin.test.verify#verify.*",
            "org.koin.test.check#checkModules",
            "io.kotest.matchers#should.*",
            "io.kotest.assertions.throwables#shouldThrow.*|shouldNotThrow.*",
        )
    }
}
