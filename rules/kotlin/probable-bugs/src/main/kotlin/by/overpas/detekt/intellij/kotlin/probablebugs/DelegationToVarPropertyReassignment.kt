package by.overpas.detekt.intellij.kotlin.probablebugs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

internal class DelegationToVarPropertyReassignment(
    private val parameter: KtParameter,
    private val containingClass: KtClass,
) {

    private val KtNameReferenceExpression.isAssigned: Boolean
        get() {
            val target = getQualifiedExpressionForSelectorOrThis()
            return when (val parent = target.parent) {
                is KtBinaryExpression -> parent.left == target && parent.operationToken in KtTokens.ALL_ASSIGNMENTS
                is KtUnaryExpression -> parent.operationToken in KtTokens.INCREMENT_AND_DECREMENT
                else -> false
            }
        }

    val isPresent: Boolean
        get() = containingClass
            .collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == parameter.name }
            .any { it.isAssigned && it.delegationToVarPropertyTarget == parameter }
}

internal val KtDelegatedSuperTypeEntry.delegationToVarPropertyParameter: KtParameter?
    get() {
        val delegate = delegateExpression as? KtNameReferenceExpression
        val parameter = delegate?.delegationToVarPropertyTarget as? KtParameter
        return parameter?.takeIf { it.isMutable }
    }

private val KtNameReferenceExpression.delegationToVarPropertyTarget: PsiElement?
    get() = analyze(this) {
        references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }?.psi
    }
