package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lang.BinaryOperationPrecedence
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtWhenEntryGuard
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class UnclearPrecedenceOfBinaryExpressionNode(
    val expression: KtExpression,
    operation: KtSimpleNameExpression,
    private val operands: List<PsiElement?>,
) {

    private val precedence = BinaryOperationPrecedence.entries
        .firstOrNull { operation.getReferencedNameElementType() in it.tokens }

    fun isRoot(): Boolean =
        expression.parents.firstOrNull { it !is KtParenthesizedExpression }?.unclearPrecedenceNode() == null

    fun isUnclear(): Boolean {
        val isOrInWhenGuard = expression is KtBinaryExpression &&
            expression.parent is KtWhenEntryGuard &&
            expression.operationToken == KtTokens.OROR
        return isOrInWhenGuard ||
            hasUnclearParent() ||
            operands.any { KtPsiUtil.deparenthesize(it as? KtExpression)?.unclearPrecedenceNode()?.isUnclear() == true }
    }

    private fun hasUnclearParent(): Boolean {
        val parentPrecedence = expression.parent?.unclearPrecedenceNode()?.precedence
        return parentPrecedence != null && parentPrecedence in unclearParents[precedence].orEmpty()
    }

    private companion object {
        val unclearParents = mapOf(
            BinaryOperationPrecedence.ELVIS to setOf(
                BinaryOperationPrecedence.EQUALITY,
                BinaryOperationPrecedence.COMPARISON,
                BinaryOperationPrecedence.IN_OR_IS,
            ),
            BinaryOperationPrecedence.INFIX to setOf(BinaryOperationPrecedence.ELVIS),
            BinaryOperationPrecedence.ADDITIVE to setOf(BinaryOperationPrecedence.ELVIS),
            BinaryOperationPrecedence.MULTIPLICATIVE to setOf(BinaryOperationPrecedence.ELVIS),
        )
    }
}
