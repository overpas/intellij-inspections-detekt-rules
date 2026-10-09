package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal fun KtExpression.canReuseBooleanExpression(): Boolean =
    !hasNonConstantComparison() && !hasNonSimpleNegation()

internal fun KtExpression.canReuseAsBooleanBranch(hasCommentedBranch: Boolean): Boolean =
    !isBooleanConstant &&
        (
            isSimpleBooleanExpression() ||
                (hasCommentedBranch && isNotNullableBoolean() && !hasNonConstantComparison() && !hasNonSimpleNegation())
            )

private fun KtExpression.hasNonConstantComparison(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtPrefixExpression ->
            expression.operationToken == KtTokens.EXCL && expression.baseExpression?.hasNonConstantComparison() == true

        is KtBinaryExpression -> when (expression.operationToken) {
            in REDUNDANT_IF_LOGICAL_OPERATIONS -> listOf(
                expression.left,
                expression.right,
            ).any { it?.hasNonConstantComparison() == true }

            in REDUNDANT_IF_COMPARISON_OPERATIONS -> listOf(expression.left, expression.right).all {
                it != null && it.isSimpleOperand() && !it.isSimpleConstantOperand()
            }

            else -> false
        }

        else -> false
    }

private fun KtExpression.hasNonSimpleNegation(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtPrefixExpression ->
            expression.operationToken == KtTokens.EXCL && expression.baseExpression?.isSimpleBooleanExpression() != true

        is KtBinaryExpression ->
            expression.operationToken in REDUNDANT_IF_LOGICAL_OPERATIONS &&
                listOf(expression.left, expression.right).any { it?.hasNonSimpleNegation() == true }

        else -> false
    }
