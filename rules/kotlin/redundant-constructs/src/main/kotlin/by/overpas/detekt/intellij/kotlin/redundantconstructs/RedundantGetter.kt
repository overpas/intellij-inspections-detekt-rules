package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import com.intellij.psi.PsiComment
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

@IntellijInspection("RedundantGetter")
class RedundantGetter(config: Config) :
    Rule(
        config,
        "A getter that only returns the backing field does nothing. Remove the getter or its body.",
    ) {

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        if (!accessor.isGetter || accessor.anyDescendantOfType<PsiComment>()) return
        val isDeletable = accessor.canBeCompletelyDeleted()
        val body = accessor.bodyExpression
        val returned = if (body is KtBlockExpression) {
            (body.statements.singleOrNull() as? KtReturnExpression)?.returnedExpression
        } else {
            body
        }
        val isRedundant = if (body == null) isDeletable else (returned as? KtNameReferenceExpression)?.text == FIELD
        if (!isRedundant) return
        val message = if (isDeletable) "Redundant getter" else "Redundant getter body"
        report(Finding(Entity.from(accessor), message))
    }

    private fun KtPropertyAccessor.canBeCompletelyDeleted(): Boolean =
        modifierList == null ||
            (
                annotationEntries.isEmpty() &&
                    !hasModifier(KtTokens.EXTERNAL_KEYWORD) &&
                    visibilityModifierTypeOrDefault() == property.visibilityModifierTypeOrDefault()
                )

    private companion object {
        const val FIELD = "field"
    }
}
