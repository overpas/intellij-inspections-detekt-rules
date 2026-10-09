package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

context(session: KaSession)
internal fun PsiElement.joinReferencedSymbols(): List<KaSymbol> =
    with(session) {
        collectDescendantsOfType<KtNameReferenceExpression>().mapNotNull { name ->
            name.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        }
    }

internal fun KtBinaryExpression.joinAssignedReference(): KtNameReferenceExpression? =
    takeIf { operationToken == KtTokens.EQ }?.left?.let { target ->
        target as? KtNameReferenceExpression
            ?: (target as? KtDotQualifiedExpression)?.selectorExpression as? KtNameReferenceExpression
    }
