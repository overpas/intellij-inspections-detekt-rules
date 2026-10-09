package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression

internal class ConvertTwoComparisonsToRangeCheckTypes(
    private val expression: KtBinaryExpression,
    private val bounds: ConvertTwoComparisonsToRangeCheckBounds,
) {

    val rangeText: String?
        get() = analyze(expression) {
            val valueType = bounds.value.expressionType?.run {
                lowerBoundIfFlexible().takeIf { it.isSubtypeOf(StandardClassIds.Comparable) }
            }
            val min = valueType?.let { ConvertTwoComparisonsToRangeCheckOperand(this, bounds.min, it) }
            val max = valueType?.let { ConvertTwoComparisonsToRangeCheckOperand(this, bounds.max, it) }
            val isExclusiveMinSupported = valueType != null &&
                ConvertTwoComparisonsToRangeCheckKind.from(valueType).isIntegralOrChar &&
                min?.kind?.isIntegralOrChar == true
            val lower = when {
                min?.text == null -> null
                !bounds.isMinExclusive -> min.text
                !isExclusiveMinSupported -> null
                bounds.min is KtConstantExpression -> bounds.min.text
                else -> "(${bounds.min.text} + 1)"
            }
            val operator = if (bounds.isMaxExclusive) "..<" else ".."
            max?.text?.let { upper -> lower?.let { "${bounds.value.text} in $it$operator$upper" } }
        }
}
