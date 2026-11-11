package by.overpas.detekt.intellij.kotlin.styleissues.unclearprecedenceofbinaryexpression

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

    private val token = operation.getReferencedNameElementType()

    private val precedence = BinaryOperationPrecedence.entries.firstOrNull { token in it.tokens }

    fun isRoot(): Boolean =
        expression.parents.firstOrNull { it !is KtParenthesizedExpression }?.unclearPrecedenceNode() == null

    fun isUnclear(areObviousCasesReported: Boolean): Boolean {
        val isOrInWhenGuard = expression is KtBinaryExpression &&
            expression.parent is KtWhenEntryGuard &&
            expression.operationToken == KtTokens.OROR
        return isOrInWhenGuard ||
            hasUnclearParent(areObviousCasesReported) ||
            operands.any { operand ->
                KtPsiUtil.deparenthesize(operand as? KtExpression)
                    ?.unclearPrecedenceNode()
                    ?.isUnclear(areObviousCasesReported) == true
            }
    }

    private fun hasUnclearParent(areObviousCasesReported: Boolean): Boolean {
        val parent = expression.parent?.unclearPrecedenceNode() ?: return false
        return parent.precedence in unclearParents[precedence].orEmpty() ||
            (areObviousCasesReported && parent.token != token)
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
