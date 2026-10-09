package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal class DestructuringDeclarationCandidate(private val declaration: KtDeclaration) {

    private val scope: PsiElement? = when (declaration) {
        is KtFunctionLiteral -> declaration
        is KtParameter -> declaration.parent as? KtForExpression ?: declaration.parent?.parent
        else -> declaration.parent
    }

    private val variableName = if (declaration is KtFunctionLiteral) "it" else declaration.name

    val highlight: PsiElement? = when (declaration) {
        is KtFunctionLiteral -> declaration.lBrace.takeUnless { declaration.hasParameterSpecification() }

        is KtParameter -> declaration.nameIdentifier.takeIf {
            declaration.parent is KtForExpression || declaration.parent?.parent is KtFunctionLiteral
        }

        is KtProperty -> declaration.nameIdentifier.takeIf { declaration.isLocal }

        else -> null
    }

    context(session: KaSession)
    fun isDestructurable(): Boolean =
        with(session) {
            val symbol = declaration.symbol.let { symbol ->
                if (symbol is KaFunctionSymbol) symbol.valueParameters.singleOrNull() else symbol as? KaCallableSymbol
            }
            val components = symbol?.run { DestructuringDeclarationComponents(returnType).indices() }
            val namesakes = scope?.collectDescendantsOfType<KtNameReferenceExpression> {
                it.getReferencedName() == variableName
            }
            val references = namesakes?.filter { namesake ->
                namesake.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } ==
                    symbol
            }
            components != null &&
                references != null &&
                DestructuringDeclarationUsages(references.map { DestructuringDeclarationUsage(it, components) })
                    .isDestructurable()
        }
}
