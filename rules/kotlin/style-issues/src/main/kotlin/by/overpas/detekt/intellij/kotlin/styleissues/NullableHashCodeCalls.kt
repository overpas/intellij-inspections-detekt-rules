package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression

private val nullableHashCodeCallableId = CallableId(StandardClassIds.Any, StandardNames.HASHCODE_NAME)

internal fun KtBinaryExpression.nullableHashCodeCall(): KtCallExpression? {
    val zero = KtPsiUtil.deparenthesize(right) as? KtConstantExpression
    val isZeroDefault = zero != null &&
        zero.node.elementType == KtNodeTypes.INTEGER_CONSTANT &&
        zero.text.toIntOrNull() == 0
    val safeCall = KtPsiUtil.safeDeparenthesize(left ?: return null) as? KtSafeQualifiedExpression
    return (safeCall?.selectorExpression as? KtCallExpression)?.takeIf { call ->
        operationToken == KtTokens.ELVIS &&
            isZeroDefault &&
            call.calleeExpression?.text == StandardNames.HASHCODE_NAME.asString() &&
            call.valueArguments.isEmpty()
    }
}

internal fun KtCallExpression.isNullableHashCodeCall(): Boolean =
    analyze(this) {
        val receiverType = (parent as? KtSafeQualifiedExpression)?.run { receiverExpression.expressionType }
        val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        val callableIds = symbol?.run { listOf(callableId) + allOverriddenSymbols.map { it.callableId } }.orEmpty()
        receiverType?.isNullable == true && nullableHashCodeCallableId in callableIds
    }
