package by.overpas.detekt.intellij.kotlin.otherproblems

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtVariableDeclaration

context(session: KaSession)
internal fun KtNameReferenceExpression.valInitializerCalls(): List<KtCallExpression> =
    with(session) {
        val symbol = references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        val declaration = symbol?.psi as? KtVariableDeclaration
        declaration?.takeUnless { it.isVar }?.children.orEmpty().mapNotNull { it.setArgumentCallOrNull() }
    }

internal fun PsiElement.setArgumentCallOrNull(): KtCallExpression? =
    when (this) {
        is KtCallExpression -> this
        is KtQualifiedExpression -> selectorExpression as? KtCallExpression
        else -> null
    }
