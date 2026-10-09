package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal val REPLACE_MAP_INDEXED_FQ_NAME = FqName("kotlin.collections.mapIndexed")

internal fun KtCallExpression.mapIndexedGeneratorCandidate(): KtFunction? {
    val function = when (val argument = valueArguments.singleOrNull()?.getArgumentExpression()) {
        is KtLambdaExpression -> argument.functionLiteral
        is KtNamedFunction -> argument
        is KtLabeledExpression -> (argument.baseExpression as? KtLambdaExpression)?.functionLiteral
        else -> null
    }
    return function?.takeIf { it.valueParameters.size == 2 && hasMapIndexedCallee() }
}

private fun KtCallExpression.hasMapIndexedCallee(): Boolean {
    val calleeText = calleeExpression?.text
    return calleeText == REPLACE_MAP_INDEXED_FQ_NAME.shortName().asString() ||
        containingKtFile.importDirectives.any {
            it.importedFqName == REPLACE_MAP_INDEXED_FQ_NAME && it.aliasName == calleeText
        }
}

internal fun KtParameter.mapIndexedDeclarations(): List<KtNamedDeclaration> =
    destructuringDeclaration?.entries ?: listOf(this)

context(session: KaSession)
internal fun KtFunction.mapIndexedReferences(declaration: KtNamedDeclaration): Boolean =
    collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == declaration.name }.any { name ->
        with(session) {
            name.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }?.psi
        } == declaration
    }
