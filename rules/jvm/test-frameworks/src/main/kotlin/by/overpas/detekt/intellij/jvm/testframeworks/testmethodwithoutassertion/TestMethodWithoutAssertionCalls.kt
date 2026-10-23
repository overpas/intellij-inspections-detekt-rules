package by.overpas.detekt.intellij.jvm.testframeworks.testmethodwithoutassertion

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtElement

context(session: KaSession, matcher: TestMethodWithoutAssertionMatcher)
internal fun KtElement.isAssertionCall(): Boolean {
    val isInfixCall = this is KtBinaryExpression &&
        operationReference.getReferencedNameElementType() == KtTokens.IDENTIFIER
    val function = if (this is KtCallElement || isInfixCall) resolveFunction() else null
    return function != null && matcher.matches(function)
}

context(session: KaSession)
internal fun KtElement.resolveFunction(): KaFunctionSymbol? =
    with(session) { resolveToCall()?.singleFunctionCallOrNull()?.symbol }
