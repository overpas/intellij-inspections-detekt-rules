package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaDeclarationSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

context(session: KaSession)
internal fun KtExpression.isReplaceWithCandidate(): Boolean =
    (collectDescendantsOfType<KtElement>() + this).none { element ->
        element is KtReturnExpression ||
            element is KtDeclaration ||
            (element is KtBlockExpression && element.statements.size > 1) ||
            (element is KtSimpleNameExpression && element.referencesPrivateSymbol())
    }

context(session: KaSession)
private fun KtSimpleNameExpression.referencesPrivateSymbol(): Boolean =
    with(session) {
        val symbol = references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        (symbol as? KaDeclarationSymbol)?.visibility == KaSymbolVisibility.PRIVATE
    }
