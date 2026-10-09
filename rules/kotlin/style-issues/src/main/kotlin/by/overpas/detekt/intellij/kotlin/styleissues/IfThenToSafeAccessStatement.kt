package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtOperationExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal fun KtExpression.ifThenSingleStatement(): KtExpression? {
    val inner = KtPsiUtil.safeDeparenthesize(this, true)
    val statement = (inner as? KtBlockExpression)
        ?.run { statements.singleOrNull() }
        ?.let { single -> KtPsiUtil.safeDeparenthesize(single, true).takeUnless { it is KtLambdaExpression } }
    return if (inner is KtBlockExpression) statement else inner
}

internal fun KtExpression?.isIfThenNullLiteral(): Boolean =
    this?.run { (ifThenSingleStatement() ?: this).node.elementType } == KtNodeTypes.NULL

internal fun KtExpression.isIfThenSimplifiableTo(other: KtExpression): Boolean =
    (ifThenSingleStatement() ?: this).text == other.text

internal fun KtOperationExpression.isPositiveIfThenCheck(): Boolean? =
    when (this) {
        is KtIsExpression -> !isNegated

        is KtBinaryExpression -> when (operationToken) {
            KtTokens.EXCLEQ -> true
            KtTokens.EQEQ -> false
            else -> null
        }

        else -> null
    }

internal fun KtOperationExpression.ifThenCheckedExpression(): KtExpression? =
    when (this) {
        is KtIsExpression -> leftHandSide
        is KtBinaryExpression -> ifThenComparedToNull()
        else -> null
    }

private fun KtBinaryExpression.ifThenComparedToNull(): KtExpression? {
    val isComparison = operationToken == KtTokens.EQEQ || operationToken == KtTokens.EXCLEQ
    val isLeftNull = left.isIfThenNullLiteral()
    val isRightNull = right.isIfThenNullLiteral()
    return (if (isLeftNull) right else left).takeIf { isComparison && isLeftNull != isRightNull }
}
