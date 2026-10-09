package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

private val readLineCallableId = CallableId(FqName("kotlin.io"), Name.identifier("readLine"))

internal fun KtCallExpression.isReadLineCall(): Boolean =
    analyze(this) {
        resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } == readLineCallableId
    }

internal fun KtCallExpression.readLineReplacementTarget(): KtExpression {
    val qualifiedOrCall = getQualifiedExpressionForSelectorOrThis()
    val notNullAssertion = (qualifiedOrCall.parent as? KtPostfixExpression)
        ?.takeIf { it.operationToken == KtTokens.EXCLEXCL }
    return notNullAssertion ?: qualifiedOrCall
}
