package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression

private val ASSERT_FUNCTION_OWNERS = setOf(
    "junit.framework.Assert",
    "junit.framework.TestCase",
    "org.junit.Assert",
    "org.junit.jupiter.api.Assertions",
    "org.testng.Assert",
    "org.testng.AssertJUnit",
    "kotlin.test",
)

context(session: KaSession)
internal fun KtCallExpression.misorderedAssertMethodName(): String? {
    val call = with(session) { resolveToCall()?.successfulFunctionCallOrNull() }
    val callableId = call?.run { symbol.callableId }
    val methodName = callableId?.run { callableName.asString() }
    val owner = callableId?.run { asSingleFqName().parent().asString() }
    val expected = call?.assertArgument("expected")
    val actual = call?.assertArgument("actual")
    val isAssertion = methodName in MISORDERED_ASSERT_METHOD_NAMES && owner in ASSERT_FUNCTION_OWNERS
    val isMisordered = expected != null &&
        actual != null &&
        !expected.isExpectedLikeAssertArgument(isActual = false) &&
        actual.isExpectedLikeAssertArgument(isActual = true)
    return methodName?.takeIf { isAssertion && isMisordered }
}

private fun KaFunctionCall<*>.assertArgument(name: String): KtExpression? =
    valueArgumentMapping.entries.firstOrNull { it.value.symbol.name.asString() == name }?.key
