package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal fun KtSimpleNameExpression.isCompanionReferenceCandidate(): Boolean {
    val qualified = parent as? KtDotQualifiedExpression
    val selector = qualified?.selectorExpression
    val isSelectorCandidate = this == selector && qualified.parent is KtDotQualifiedExpression
    val isReceiverCandidate = qualified != null &&
        this != selector &&
        text != (selector as? KtNameReferenceExpression)?.text
    return qualified?.getStrictParentOfType<KtImportDirective>() == null &&
        (isSelectorCandidate || isReceiverCandidate)
}

internal fun KtSimpleNameExpression.isCompanionNamedAsReference(): Boolean {
    val reference = this
    return analyze(reference) {
        val symbol = reference.mainReference.resolveToSymbol() as? KaNamedClassSymbol
        val companion = symbol?.takeIf { it.classKind == KaClassKind.COMPANION_OBJECT }
        (companion?.psi as? KtObjectDeclaration)?.name == reference.text
    }
}

internal fun KtSimpleNameExpression.keepsCallTargetWithoutIt(): Boolean {
    val qualified = parent as? KtDotQualifiedExpression
    val outer = (qualified?.parent as? KtDotQualifiedExpression)?.takeIf { this == qualified.selectorExpression }
    val target = outer?.selectorExpression ?: qualified?.selectorExpression
    if (qualified == null || target == null) return false
    val simplifiedText = outer?.let { "${qualified.receiverExpression.text}.${target.text}" } ?: target.text
    val oldTarget = analyze(target) { callTarget(target) }
    return oldTarget != null && oldTarget == resolveInPlace(simplifiedText)
}

private fun KtElement.resolveInPlace(text: String): PsiElement? {
    val fragment = KtPsiFactory(project).createExpressionCodeFragment(text, this)
    val expression = fragment.getContentElement() ?: return null
    return analyze(expression) { callTarget(expression) }
}

private fun KaSession.callTarget(element: KtElement): PsiElement? =
    element.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()?.let { it.symbol.psi }
