package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

internal fun KtPropertyAccessor.isSetterDeletable(): Boolean =
    modifierList == null ||
        (
            annotationEntries.isEmpty() &&
                !hasModifier(KtTokens.EXTERNAL_KEYWORD) &&
                visibilityModifierTypeOrDefault() == property.visibilityModifierTypeOrDefault()
            )

internal fun KtPropertyAccessor.setterOnlyAssignsField(): Boolean {
    val assignment = (bodyExpression as? KtBlockExpression)?.run { statements.singleOrNull() } as? KtBinaryExpression
    val parameter = valueParameters.singleOrNull()
    return assignment != null &&
        parameter != null &&
        assignment.operationToken == KtTokens.EQ &&
        (assignment.left as? KtNameReferenceExpression)?.text == KtTokens.FIELD_KEYWORD.value &&
        (assignment.right as? KtNameReferenceExpression)?.getReferencedName() == parameter.name
}

internal fun KtProperty.overriddenSourceProperty(): KtProperty? =
    analyze(this) {
        val property = symbol as? KaPropertySymbol
        (property?.run { directlyOverriddenSymbols.firstOrNull() } as? KaPropertySymbol)?.psi
    } as? KtProperty

internal fun KtModifierKeywordToken.setterVisibilityRank(): Int =
    when (this) {
        KtTokens.PRIVATE_KEYWORD -> 0
        KtTokens.PUBLIC_KEYWORD -> 2
        else -> 1
    }
