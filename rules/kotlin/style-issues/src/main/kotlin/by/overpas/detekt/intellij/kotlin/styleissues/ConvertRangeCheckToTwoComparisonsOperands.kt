package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val CONVERT_RANGE_CHECK_OPERATORS = setOf("..", "..<", "until", "downTo")

private val CONVERT_RANGE_CHECK_FUNCTIONS = setOf("rangeTo", "rangeUntil")

private val CONVERT_RANGE_CHECK_TOKENS = setOf(KtTokens.IN_KEYWORD, KtTokens.NOT_IN)

internal fun KtBinaryExpression.rangeCheckOperands(): List<KtExpression>? {
    val bounds = when (val range = right) {
        is KtBinaryExpression -> listOf(range.left, range.right)
            .takeIf { range.operationReference.text in CONVERT_RANGE_CHECK_OPERATORS }

        is KtDotQualifiedExpression -> (range.selectorExpression as? KtCallExpression)
            ?.takeIf { it.calleeExpression?.text in CONVERT_RANGE_CHECK_FUNCTIONS }
            ?.let { listOf(range.receiverExpression, it.valueArguments.singleOrNull()?.getArgumentExpression()) }

        else -> null
    }
    val operands = bounds?.let { listOf(left) + it }.orEmpty().mapNotNull { operand ->
        operand?.takeIf { it is KtConstantExpression || it is KtNameReferenceExpression }
    }
    val isRangeCheck = operationToken in CONVERT_RANGE_CHECK_TOKENS && parent !is KtForExpression
    return operands.takeIf { isRangeCheck && it.size == 3 }
}
