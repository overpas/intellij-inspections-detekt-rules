package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectLiteralExpression
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

internal class UseExpressionBodyBlock(private val block: KtBlockExpression) {

    private val statement: KtExpression? = block.statements.singleOrNull()

    fun isConvertible(): Boolean {
        val value = (statement as? KtReturnExpression)?.returnedExpression ?: statement
        val hasInnerReturn = value?.anyDescendantOfType<KtReturnExpression>(
            canGoInside = { it !is KtFunctionLiteral && it !is KtNamedFunction && it !is KtPropertyAccessor },
        )
        return block.statements.size <= 1 && hasInnerReturn != true
    }

    fun highlighted(): PsiElement {
        val target = (statement as? KtQualifiedExpression)?.selectorExpression ?: statement
        return when (target) {
            is KtReturnExpression -> target.returnKeyword
            is KtCallExpression -> target.calleeExpression
            is KtObjectLiteralExpression -> target.objectDeclaration.getObjectKeyword()
            else -> target
        } ?: block
    }

    context(session: KaSession)
    fun subject(): String? =
        when (val statement = statement) {
            null -> "block body"

            is KtReturnExpression -> when {
                statement.returnedExpression is KtWhenExpression -> "'return when'"
                '\n' !in statement.text -> "one-line return"
                else -> "return"
            }

            else -> "block body".takeIf { statement.hasUnitOrNothingValue() }
        }

    private companion object {

        context(session: KaSession)
        fun KtExpression.hasUnitOrNothingValue(): Boolean {
            val isAssignment = this is KtBinaryExpression && operationToken in KtTokens.ALL_ASSIGNMENTS
            val type = with(session) { expressionType }
            return when {
                this is KtDeclaration || this is KtLoopExpression || isAssignment || type == null -> false
                with(session) { type.isNothingType } -> true
                else -> with(session) { type.isUnitType } && UseExpressionBodyBranches(this).areExhaustive()
            }
        }
    }
}
