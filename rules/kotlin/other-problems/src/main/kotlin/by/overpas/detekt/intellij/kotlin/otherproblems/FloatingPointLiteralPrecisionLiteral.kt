package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.psi.KtConstantExpression

private val floatingPointLiteralPrecisionIgnoredChars = Regex("[_fF]")

internal fun KtConstantExpression.exceedsFloatingPointPrecision(isFloat: Boolean): Boolean {
    val literal = text.replace(floatingPointLiteralPrecisionIgnoredChars, "")
    val rounded = if (isFloat) literal.toFloatOrNull()?.toString() else literal.toDoubleOrNull()?.toString()
    val exactValue = literal.toBigDecimalOrNull()
    val roundedValue = rounded?.toBigDecimalOrNull()
    return exactValue != null && roundedValue != null && exactValue.compareTo(roundedValue) != 0
}
