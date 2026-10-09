package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val replaceStringFormatWithLiteralFunctions = setOf("kotlin.text.format", "java.lang.String.format")

private val replaceStringFormatWithLiteralFormattable = ClassId.fromString("java/util/Formattable")

internal fun KtCallExpression.replaceStringFormatTarget(): KtExpression? {
    val qualified = parent as? KtQualifiedExpression
    return takeIf { calleeExpression?.text == "format" }
        ?.takeIf { qualified?.receiverExpression?.text?.endsWith("String") != false }
        ?.let { qualified ?: it }
}

internal fun KtCallExpression.hasReplaceStringFormatArgs(): Boolean {
    val arguments = valueArguments.mapNotNull { it.getArgumentExpression() }
    val format = arguments.firstOrNull()?.text.orEmpty()
    val placeholders = format.indices.filter { format[it] == '%' }
    return arguments.size > 1 &&
        !format.startsWith("\"\"\"") &&
        placeholders.size == arguments.size - 1 &&
        placeholders.all { format.getOrNull(it + 1) == 's' }
}

context(session: KaSession)
internal fun KtCallExpression.isReplaceStringFormatCall(): Boolean =
    with(session) {
        val fqName = resolveToCall()
            ?.successfulFunctionCallOrNull()
            ?.symbol
            ?.callableId
            ?.run { asSingleFqName().asString() }
        val hasFormattableArgument = valueArguments.drop(1).any { argument ->
            val type = argument.getArgumentExpression()?.expressionType
            type != null &&
                (type.allSupertypes + type).any { it.isClassType(replaceStringFormatWithLiteralFormattable) }
        }
        fqName in replaceStringFormatWithLiteralFunctions && !hasFormattableArgument
    }
