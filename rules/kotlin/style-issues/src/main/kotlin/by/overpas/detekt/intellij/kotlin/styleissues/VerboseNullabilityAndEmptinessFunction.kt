package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.name.FqName

private const val COLLECTIONS = "kotlin.collections"
private const val TEXT = "kotlin.text"
private const val NULL_OR_EMPTY = "isNullOrEmpty"
private const val NULL_OR_BLANK = "isNullOrBlank"

internal enum class VerboseNullabilityAndEmptinessFunction(
    val functionName: String,
    val isPositive: Boolean,
    val replacementName: String,
    private val owners: List<String>,
) {
    IS_EMPTY(
        functionName = "isEmpty",
        isPositive = false,
        replacementName = NULL_OR_EMPTY,
        owners = listOf("$COLLECTIONS.Collection", "$COLLECTIONS.Map", COLLECTIONS, TEXT),
    ),
    IS_BLANK(
        functionName = "isBlank",
        isPositive = false,
        replacementName = NULL_OR_BLANK,
        owners = listOf(TEXT),
    ),
    IS_NOT_EMPTY(
        functionName = "isNotEmpty",
        isPositive = true,
        replacementName = NULL_OR_EMPTY,
        owners = listOf(COLLECTIONS, TEXT),
    ),
    IS_NOT_BLANK(
        functionName = "isNotBlank",
        isPositive = true,
        replacementName = NULL_OR_BLANK,
        owners = listOf(TEXT),
    ),
    ;

    fun isDeclaredAs(fqName: FqName?): Boolean =
        owners.any { "$it.$functionName" == fqName?.asString() }
}
