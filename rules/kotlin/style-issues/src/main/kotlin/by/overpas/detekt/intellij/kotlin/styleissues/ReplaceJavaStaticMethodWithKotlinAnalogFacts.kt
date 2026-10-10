package by.overpas.detekt.intellij.kotlin.styleissues

internal data class ReplaceJavaStaticMethodWithKotlinAnalogFacts(
    val argumentCount: Int,
    val isFirstArgumentNullable: Boolean?,
    val hasValidRadix: Boolean,
    val isSystemOut: Boolean,
    val hasLambdaSecondArgument: Boolean,
    val isFirstArgumentMutableList: Boolean,
    val isFirstArgumentChar: Boolean,
) {

    val hasTwoNonNullArguments: Boolean
        get() = argumentCount == 2 && isFirstArgumentNullable == false

    val isPrimitiveToString: Boolean
        get() = argumentCount == 1 || (hasTwoNonNullArguments && hasValidRadix)
}
