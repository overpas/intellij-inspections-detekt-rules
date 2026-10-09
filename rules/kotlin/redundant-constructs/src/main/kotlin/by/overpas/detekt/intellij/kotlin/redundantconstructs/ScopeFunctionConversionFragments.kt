package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.startOffset

internal fun KtCallExpression.scopeFunctionFragmentSource(
    calleeName: String,
    name: String,
): Pair<String, PsiElement>? {
    val lambda = lambdaArguments.firstOrNull()?.getLambdaExpression() ?: return null
    val qualified = parent as? KtQualifiedExpression
    val withReceiver = valueArguments.firstOrNull()?.getArgumentExpression()?.text
    return when {
        calleeName == SCOPE_FUNCTION_WITH -> withReceiver?.let { "$it.$name${lambda.text}" to parent }

        name == SCOPE_FUNCTION_WITH && qualified != null ->
            "$SCOPE_FUNCTION_WITH(${qualified.receiverExpression.text})${lambda.textWithoutParameters}" to
                qualified.parent

        else -> renamedCallText(name) to parent
    }
}

private fun KtCallExpression.renamedCallText(name: String): String {
    val callee = calleeExpression ?: return text
    val outer = parent as? KtQualifiedExpression ?: this
    val start = callee.startOffset - outer.startOffset
    return outer.text.replaceRange(start, start + callee.textLength, name)
}

private val KtLambdaExpression.textWithoutParameters: String
    get() {
        val parameters = functionLiteral.valueParameterList ?: return text
        val start = parameters.startOffset - startOffset
        return text.removeRange(start, start + parameters.textLength)
    }
