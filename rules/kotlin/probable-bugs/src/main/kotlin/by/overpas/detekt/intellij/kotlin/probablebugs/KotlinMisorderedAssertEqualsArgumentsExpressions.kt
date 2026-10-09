package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

context(session: KaSession)
internal fun KtExpression.isExpectedLikeAssertArgument(isActual: Boolean): Boolean {
    val expression = KtPsiUtil.safeDeparenthesize(this)
    val constant = with(session) { expression.evaluate() }
    return (constant != null && constant !is KaConstantValue.ErrorValue) || expression.isExpectedLikeByKind(isActual)
}

context(session: KaSession)
private fun KtExpression.isExpectedLikeByKind(isActual: Boolean): Boolean {
    val qualified = this as? KtDotQualifiedExpression
    val selector = qualified?.selectorExpression
    return when {
        this is KtConstantExpression -> true
        this is KtStringTemplateExpression -> !hasInterpolation()
        this is KtNameReferenceExpression -> isExpectedLikeAssertReference(isActual)
        this is KtCallExpression -> isExpectedLikeAssertCall(receiver = null, isActual = isActual)
        selector is KtNameReferenceExpression -> selector.isExpectedLikeAssertReference(isActual)
        selector is KtCallExpression -> selector.isExpectedLikeAssertCall(qualified.receiverExpression, isActual)
        else -> false
    }
}
