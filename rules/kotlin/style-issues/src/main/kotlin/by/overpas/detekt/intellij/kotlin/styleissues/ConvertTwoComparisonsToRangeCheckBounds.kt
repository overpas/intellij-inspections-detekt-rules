package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtExpression

internal data class ConvertTwoComparisonsToRangeCheckBounds(
    val value: KtExpression,
    val min: KtExpression,
    val isMinExclusive: Boolean,
    val max: KtExpression,
    val isMaxExclusive: Boolean,
)
