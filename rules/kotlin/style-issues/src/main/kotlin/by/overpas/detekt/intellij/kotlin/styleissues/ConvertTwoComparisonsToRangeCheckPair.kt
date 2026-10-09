package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

internal class ConvertTwoComparisonsToRangeCheckPair(expression: KtBinaryExpression) {

    private val isInverse = expression.operationToken == KtTokens.OROR

    private val isLogical = isInverse || expression.operationToken == KtTokens.ANDAND

    private val first = (expression.left as? KtBinaryExpression)?.let { binary ->
        ConvertTwoComparisonsToRangeCheckComparison(binary, isInverse).takeIf { it.isValid }
    }

    private val second = (expression.right as? KtBinaryExpression)?.let { binary ->
        ConvertTwoComparisonsToRangeCheckComparison(binary, isInverse).takeIf { it.isValid }
    }

    val bounds: ConvertTwoComparisonsToRangeCheckBounds?
        get() {
            val (low, high) = when {
                !isLogical || first == null || second == null -> null
                first.isLowBoundFor(second) -> first to second
                second.isLowBoundFor(first) -> second to first
                else -> null
            } ?: return null
            val value = low.greater
            val min = low.lesser?.takeIf { it is KtConstantExpression || it is KtNameReferenceExpression }
            val max = high.greater?.takeIf { it is KtConstantExpression || it is KtNameReferenceExpression }
            return if (value != null && min != null && max != null) {
                ConvertTwoComparisonsToRangeCheckBounds(
                    value = value,
                    min = min,
                    isMinExclusive = low.isStrict,
                    max = max,
                    isMaxExclusive = high.isStrict,
                )
            } else {
                null
            }
        }
}
