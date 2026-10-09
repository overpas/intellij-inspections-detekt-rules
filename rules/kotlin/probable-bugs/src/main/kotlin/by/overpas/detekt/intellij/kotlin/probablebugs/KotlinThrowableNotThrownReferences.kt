package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

context(session: KaSession)
internal fun KtProperty.hasLocalReferences(): Boolean =
    parent.anyDescendantOfType<KtSimpleNameExpression> { name ->
        name.getReferencedName() == this.name &&
            name.references
                .filterIsInstance<KtReference>()
                .any { with(session) { it.resolveToSymbol() }?.psi == this }
    }
