package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtSuperExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal class ConvertToStringTemplateOperand(
    private val session: KaSession,
    private val plus: ConvertToStringTemplatePlus,
    private val original: KtExpression?,
) {

    private val expression: KtExpression?
        get() = original?.run {
            when (val inner = KtPsiUtil.safeDeparenthesize(this)) {
                is KtDotQualifiedExpression if
                inner.receiverExpression !is KtSuperExpression &&
                    plus.isToString(inner) -> inner.receiverExpression

                is KtLambdaExpression if inner.parent is KtLabeledExpression -> this

                else -> inner
            }
        }

    fun text(
        isBraceForced: Boolean,
        nextText: String?,
    ): String =
        when (val operand = expression) {
            null -> ""

            is KtConstantExpression ->
                ConvertToStringTemplateConstant(session, operand, isBraceForced).text ?: $$"${$${operand.text}}"

            is KtStringTemplateExpression -> ConvertToStringTemplateLiteral(operand, isBraceForced, nextText).text

            is KtNameReferenceExpression -> "$" + if (isBraceForced) "{${operand.text}}" else operand.text

            is KtThisExpression ->
                "$" + if (isBraceForced || operand.labelQualifier != null) "{${operand.text}}" else operand.text

            else -> $$"${$${operand.text}}"
        }
}
