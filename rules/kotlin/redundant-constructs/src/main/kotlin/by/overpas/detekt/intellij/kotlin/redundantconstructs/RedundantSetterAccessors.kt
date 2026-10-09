package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiComment
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

internal fun KtPropertyAccessor.isRedundantSetterAccessor(): Boolean {
    if (!isSetter || anyDescendantOfType<PsiComment>()) return false
    return if (bodyExpression == null) isSetterDeletable() else setterOnlyAssignsField()
}

internal fun KtPropertyAccessor.hasWeakerOverriddenSetter(): Boolean {
    val overridden = property
        .takeIf { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
        ?.overriddenSourceProperty()
        ?: return false
    val overriddenVisibility =
        overridden.setter?.visibilityModifierTypeOrDefault() ?: overridden.visibilityModifierTypeOrDefault()
    return overriddenVisibility.setterVisibilityRank() < visibilityModifierTypeOrDefault().setterVisibilityRank()
}
