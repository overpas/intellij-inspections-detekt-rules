package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import kotlin.math.sign

private const val EMPTY_RANGE_DOWN_TO = "downTo"

private const val EMPTY_RANGE_RANGE_TO = "rangeTo"

internal enum class EmptyRangeOperator(
    private val binaryName: String,
    private val functionName: String?,
    private val suggestions: Map<Int, String>,
) {
    RANGE_TO("..", EMPTY_RANGE_RANGE_TO, mapOf(1 to EMPTY_RANGE_DOWN_TO)),
    RANGE_UNTIL("..<", "rangeUntil", mapOf(1 to EMPTY_RANGE_DOWN_TO, 0 to EMPTY_RANGE_RANGE_TO)),
    UNTIL("until", null, mapOf(1 to EMPTY_RANGE_DOWN_TO, 0 to EMPTY_RANGE_RANGE_TO)),
    DOWN_TO(EMPTY_RANGE_DOWN_TO, null, mapOf(-1 to EMPTY_RANGE_RANGE_TO)),
    ;

    fun suggestionFor(comparison: Int): String? =
        suggestions[comparison.sign]

    companion object {
        fun from(expression: KtExpression): EmptyRangeOperator? =
            when (expression) {
                is KtBinaryExpression ->
                    entries.firstOrNull { it.binaryName == expression.operationReference.getReferencedName() }

                is KtDotQualifiedExpression ->
                    (expression.selectorExpression as? KtCallExpression)?.calleeExpression?.text
                        ?.let { name -> entries.firstOrNull { it.functionName == name } }

                else -> null
            }
    }
}
