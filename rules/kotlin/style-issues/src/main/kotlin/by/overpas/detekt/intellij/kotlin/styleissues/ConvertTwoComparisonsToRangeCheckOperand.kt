package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.kotlin.styleissues.ConvertTwoComparisonsToRangeCheckKind.FLOATING
import by.overpas.detekt.intellij.kotlin.styleissues.ConvertTwoComparisonsToRangeCheckKind.SIGNED
import by.overpas.detekt.intellij.kotlin.styleissues.ConvertTwoComparisonsToRangeCheckKind.UNSIGNED
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtExpression

internal class ConvertTwoComparisonsToRangeCheckOperand(
    private val session: KaSession,
    private val expression: KtExpression,
    private val valueType: KaType,
) {

    private val type: KaType? = with(session) { expression.expressionType?.lowerBoundIfFlexible() }

    val kind: ConvertTwoComparisonsToRangeCheckKind? = type?.let { ConvertTwoComparisonsToRangeCheckKind.from(it) }

    val text: String?
        get() = with(session) {
            val valueKind = ConvertTwoComparisonsToRangeCheckKind.from(valueType)
            when {
                type == null -> null

                type.semanticallyEquals(valueType) -> expression.text

                valueKind == FLOATING && kind == SIGNED ->
                    (expression.evaluate()?.value as? Number)?.run { "${toDouble()}" }

                valueKind == kind && kind in setOf(SIGNED, UNSIGNED, FLOATING) -> expression.text

                else -> null
            }
        }
}
