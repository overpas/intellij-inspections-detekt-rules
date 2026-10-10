package by.overpas.detekt.intellij.kotlin.styleissues

internal enum class ReplaceJavaStaticMethodWithKotlinAnalogRequirement(
    val isMet: (ReplaceJavaStaticMethodWithKotlinAnalogFacts) -> Boolean,
) {
    ANY({ true }),
    NON_NULL_RECEIVER({ it.isFirstArgumentNullable == false }),
    NULLABLE_RECEIVER({ it.argumentCount > 0 }),
    MUTABLE_LIST_RECEIVER({ it.isFirstArgumentMutableList }),
    SORTABLE_LIST({ it.isFirstArgumentMutableList && (it.argumentCount == 1 || it.hasLambdaSecondArgument) }),
    SYSTEM_OUT({ it.isSystemOut }),
    TWO_ARGUMENTS({ it.argumentCount == 2 }),
    TWO_ARGUMENTS_NON_NULL({ it.hasTwoNonNullArguments }),
    PRIMITIVE_TO_STRING({ it.isPrimitiveToString }),
    CHARACTER_TO_STRING({ it.argumentCount == 1 && it.isFirstArgumentChar }),
}
