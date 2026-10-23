package by.overpas.detekt.intellij.kotlin.styleissues.useexpressionbody

import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

internal class UseExpressionBodyReturn(private val statement: KtReturnExpression) {

    fun isOneLine(): Boolean {
        val value = statement.returnedExpression
        return value != null &&
            value !is KtWhenExpression &&
            '\n' !in statement.text &&
            !value.anyDescendantOfType<KtReturnExpression>(
                canGoInside = { it !is KtFunctionLiteral && it !is KtNamedFunction && it !is KtPropertyAccessor },
            )
    }
}
