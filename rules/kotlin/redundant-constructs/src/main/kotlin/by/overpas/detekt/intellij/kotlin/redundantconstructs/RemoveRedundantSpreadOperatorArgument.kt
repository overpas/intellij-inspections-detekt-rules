package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class RemoveRedundantSpreadOperatorArgument(private val argument: KtValueArgument) {

    fun isRedundant(): Boolean =
        when (val expression = argument.getArgumentExpression()) {
            is KtCollectionLiteralExpression -> true
            is KtCallExpression -> expression.isInPlaceArrayFactoryCall() && isSameTargetWithoutSpread()
            else -> false
        }

    private fun isSameTargetWithoutSpread(): Boolean {
        val call = argument.getStrictParentOfType<KtCallExpression>() ?: return false
        val oldTarget = call.spreadCallTarget()
        val content = KtPsiFactory(call.project)
            .createExpressionCodeFragment(call.textWithoutSpread(argument), call)
            .getContentElement()
        val newCall = (content as? KtQualifiedExpression)?.selectorExpression ?: content
        return oldTarget != null && newCall is KtCallExpression && newCall.spreadCallTarget() == oldTarget
    }
}
