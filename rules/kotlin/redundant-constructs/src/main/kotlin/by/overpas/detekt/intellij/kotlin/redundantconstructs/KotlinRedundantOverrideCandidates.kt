package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSuperExpression

private const val SUPPRESS = "Suppress"

private const val UNIT = "Unit"

private val NON_OVERRIDE_MODIFIERS = KtTokens.MODIFIER_KEYWORDS_ARRAY.toList() - KtTokens.OVERRIDE_KEYWORD

internal fun KtNamedFunction.superQualifiedExpression(): KtDotQualifiedExpression? {
    val body = bodyExpression
    val expression = if (body is KtBlockExpression) {
        when (val statement = body.statements.singleOrNull()) {
            is KtReturnExpression -> statement.returnedExpression
            is KtDotQualifiedExpression -> statement.takeIf { (typeReference?.text ?: UNIT) == UNIT }
            else -> null
        }
    } else {
        body
    }
    return expression as? KtDotQualifiedExpression
}

internal fun KtDotQualifiedExpression.superCallElement(): KtCallElement? {
    val superExpression = receiverExpression as? KtSuperExpression
    val call = selectorExpression as? KtCallElement
    return call?.takeIf { superExpression != null && superExpression.superTypeQualifier == null }
}

internal fun KtNamedFunction.hasOnlyOverrideModifier(): Boolean =
    modifierList?.let { modifiers ->
        modifiers.hasModifier(KtTokens.OVERRIDE_KEYWORD) && NON_OVERRIDE_MODIFIERS.none { modifiers.hasModifier(it) }
    } == true

internal fun KtNamedFunction.hasNonSuppressAnnotations(): Boolean =
    annotationEntries.size > 1 ||
        annotationEntries.singleOrNull()?.let { it.shortName?.asString() != SUPPRESS } == true

internal fun KtNamedFunction.isSameNameAndArguments(call: KtCallElement): Boolean {
    val arguments = call.valueArguments
    val parameters = valueParameters
    return call.calleeExpression?.text == name &&
        arguments.size == parameters.size &&
        arguments.zip(parameters).all { (argument, parameter) ->
            argument.getArgumentExpression()?.text == parameter.name
        }
}
