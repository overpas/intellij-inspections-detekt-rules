package by.overpas.detekt.intellij.kotlin.probablebugs

internal data class SuspiciousEqualsCombinationOperand(
    val name: String,
    val isIdentity: Boolean,
)
