package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

private val setConversionStdlibFunctions = setOf(
    "kotlin.collections.minus",
    "kotlin.sequences.minus",
    "kotlin.collections.minusAssign",
    "kotlin.collections.intersect",
    "kotlin.collections.subtract",
    "kotlin.collections.removeAll",
    "kotlin.collections.MutableCollection.removeAll",
    "kotlin.collections.MutableSet.removeAll",
    "kotlin.collections.retainAll",
    "kotlin.collections.MutableCollection.retainAll",
    "kotlin.collections.MutableSet.retainAll",
)

context(session: KaSession)
internal fun KtCallExpression.setConvertibleArgument(): KtExpression? =
    with(session) {
        val receiverType = getQualifiedExpressionForSelector()?.run { receiverExpression.expressionType }
        val functionName = setConversionFunctionName()
        val isApplicable = functionName != null && receiverType?.isSetConversionReceiverOf(functionName) == true
        valueArguments
            .mapNotNull { it.getArgumentExpression() }
            .firstOrNull { isApplicable && it.isConvertibleToSet() }
    }

context(session: KaSession)
internal fun KtBinaryExpression.setConvertibleOperand(): KtExpression? =
    with(session) {
        val functionName = when (operationToken) {
            KtTokens.MINUS -> "minus"
            KtTokens.MINUSEQ -> "minusAssign"
            KtTokens.IDENTIFIER -> setConversionFunctionName()
            else -> null
        }
        val receiverType = left?.expressionType
        val isApplicable = functionName != null && receiverType?.isSetConversionReceiverOf(functionName) == true
        right?.takeIf { isApplicable && it.isConvertibleToSet() }
    }

context(session: KaSession)
private fun KtElement.setConversionFunctionName(): String? =
    with(session) {
        val callableId = resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
        callableId
            ?.takeIf { it.asSingleFqName().asString() in setConversionStdlibFunctions }
            ?.run { callableName.asString() }
    }
