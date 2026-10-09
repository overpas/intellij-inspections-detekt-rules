package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.symbols.KaAnonymousFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.name
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

internal data class ImplicitThisLabel(val name: Name?)

internal fun KaSymbol.implicitThisLabel(): ImplicitThisLabel? {
    val owner = (this as? KaReceiverParameterSymbol)?.owningCallableSymbol ?: this
    val declaration = owner.psi as? KtDeclaration
    val name = when (owner) {
        is KaAnonymousFunctionSymbol -> declaration?.lambdaLabel()
        is KaClassSymbol, is KaCallableSymbol -> owner.name
        else -> return null
    }
    return declaration?.let { ImplicitThisLabel(name) }
}

private fun KtDeclaration.lambdaLabel(): Name? {
    val labeledOrArgument = parent?.parent
    val callee = (labeledOrArgument?.parent as? KtCallExpression)?.calleeExpression as? KtNameReferenceExpression
    return (labeledOrArgument as? KtLabeledExpression)?.getLabelNameAsName() ?: callee?.getReferencedNameAsName()
}
