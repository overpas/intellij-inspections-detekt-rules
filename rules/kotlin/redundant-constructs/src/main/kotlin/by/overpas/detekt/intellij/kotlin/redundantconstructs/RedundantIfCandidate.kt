package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getNextSiblingIgnoringWhitespaceAndComments
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments

private val INEQUALITY_OPERATIONS = setOf(KtTokens.LT, KtTokens.LTEQ, KtTokens.GT, KtTokens.GTEQ)

internal class RedundantIfCandidate(private val expression: KtIfExpression) {

    private val then = expression.then

    private val elseOrReturnAfterIf =
        expression.`else` ?: expression.getNextSiblingIgnoringWhitespaceAndComments() as? KtReturnExpression

    private val hasCommentedThen: Boolean
        get() = then?.hasRedundantIfComments(null) == true

    private val hasCommentedElse: Boolean
        get() = elseOrReturnAfterIf?.hasRedundantIfComments(then) == true

    private val isChained: Boolean
        get() = expression.getPrevSiblingIgnoringWhitespaceAndComments() is KtIfExpression ||
            (expression.parent as? KtContainerNodeForControlStructureBody)?.expression == expression

    private val hasFloatingPointComparison: Boolean
        get() {
            val comparison = (expression.condition as? KtBinaryExpression)
                ?.takeIf { it.operationToken in INEQUALITY_OPERATIONS }
            val left = comparison?.left
            val right = comparison?.right
            return left != null && right != null && (left.isFloatingPoint() || right.isFloatingPoint())
        }

    fun isRedundant(): Boolean {
        val condition = expression.condition
        val thenBranch = then?.redundantIfBranch()
        val elseBranch = elseOrReturnAfterIf?.redundantIfBranch()
        if (condition == null || thenBranch == null || elseBranch == null) return false
        val isIgnored = thenBranch.second != elseBranch.second ||
            isChained ||
            hasFloatingPointComparison ||
            (hasCommentedThen && hasCommentedElse)
        val values = thenBranch.first to elseBranch.first
        return !isIgnored && values.areRedundantFor(condition)
    }

    private fun Pair<KtExpression, KtExpression>.areRedundantFor(condition: KtExpression): Boolean {
        val hasCommentedBranch = hasCommentedThen || hasCommentedElse
        val hasSimpleCondition = condition.isSimpleBooleanExpression()
        val isConditionReusable = condition.canReuseBooleanExpression()
        val isReplaceableWithCondition = hasSimpleCondition ||
            (first.isSimpleBranchLiteral() && second.isSimpleBranchLiteral() && isConditionReusable)
        val isReplaceableWithBranch = hasSimpleCondition || (hasCommentedBranch && isConditionReusable)
        return (isReplaceableWithCondition && areOppositeBooleanConstants()) ||
            (isReplaceableWithBranch && pairConstantWithReusable(hasCommentedBranch))
    }
}
