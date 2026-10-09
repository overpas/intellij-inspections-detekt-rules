package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal fun KtExpression.implicitThisReference(): KtSimpleNameExpression? =
    when (this) {
        is KtNameReferenceExpression -> takeUnless { it.hasExplicitReceiverPosition() }
        is KtCallableReferenceExpression -> callableReference.takeIf { receiverExpression == null }
        else -> null
    }

private fun KtNameReferenceExpression.hasExplicitReceiverPosition(): Boolean {
    val selector = (parent as? KtCallExpression)?.takeIf { it.calleeExpression == this } ?: this
    return parent is KtThisExpression ||
        parent is KtCallableReferenceExpression ||
        (selector.parent as? KtQualifiedExpression)?.selectorExpression == selector
}
