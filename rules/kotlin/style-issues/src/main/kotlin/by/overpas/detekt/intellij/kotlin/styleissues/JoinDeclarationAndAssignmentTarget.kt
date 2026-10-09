package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal fun KtProperty.joinableAssignment(): KtBinaryExpression? {
    val container = parent as? KtElement
    val isCandidate = !hasDelegate() && !hasInitializer() && getter == null && setter == null &&
        receiverTypeReference == null && name != null && typeReference != null
    val assignments = container
        ?.takeIf { isCandidate }
        ?.collectDescendantsOfType<KtBinaryExpression> { it.joinAssignedReference()?.getReferencedName() == name }
        .orEmpty()
    val first = assignments.firstOrNull()
    val isValid = assignments.all { it.parent.isJoinableAssignmentParent(container) } &&
        assignments.drop(1).none { it.parent.parent is KtSecondaryConstructor && it.parent is KtBlockExpression }
    val isFirstStatement = container !is KtClassBody ||
        (first?.parent as? KtBlockExpression)?.run { statements.firstOrNull() } == first
    return first.takeIf { isValid && isFirstStatement }
}

private fun PsiElement?.isJoinableAssignmentParent(container: KtElement?): Boolean {
    val grandParent = this?.parent
    val isInitializerBlock = grandParent is KtAnonymousInitializer || grandParent is KtSecondaryConstructor
    return this === container || (grandParent?.parent === container && isInitializerBlock)
}
