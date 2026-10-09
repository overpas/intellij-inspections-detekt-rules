package by.overpas.detekt.intellij.kotlin.styleissues

internal data class ReplaceJavaStaticMethodWithKotlinAnalogFacts(
    val argumentCount: Int,
    val hasValidRadix: Boolean,
    val hasLambdaSecondArgument: Boolean,
    val isFirstArgumentNullable: Boolean?,
    val isFirstArgumentMutableList: Boolean,
    val isFirstArgumentChar: Boolean,
    val isSystemOut: Boolean,
)
