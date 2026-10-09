package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal val REDUNDANT_IF_LOGICAL_OPERATIONS: TokenSet = TokenSet.create(KtTokens.ANDAND, KtTokens.OROR)

internal val REDUNDANT_IF_COMPARISON_OPERATIONS: TokenSet = TokenSet.create(
    KtTokens.EQEQ,
    KtTokens.EXCLEQ,
    KtTokens.EQEQEQ,
    KtTokens.EXCLEQEQEQ,
    KtTokens.LT,
    KtTokens.LTEQ,
    KtTokens.GT,
    KtTokens.GTEQ,
    KtTokens.IN_KEYWORD,
    KtTokens.NOT_IN,
)

internal fun KtExpression.isNotNullableBoolean(): Boolean =
    analyze(this) { expressionType?.let { it.isBooleanType && !it.isMarkedNullable } == true }

internal fun KtExpression.isFloatingPoint(): Boolean =
    analyze(this) { expressionType?.run { isFloatType || isDoubleType } == true }

internal fun KtExpression.isSimpleBooleanExpression(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        null -> false

        is KtPrefixExpression ->
            expression.operationToken == KtTokens.EXCL && expression.baseExpression?.isSimpleBooleanExpression() == true

        is KtIsExpression -> expression.leftHandSide.isSimpleOperand()

        is KtBinaryExpression -> when (expression.operationToken) {
            in REDUNDANT_IF_LOGICAL_OPERATIONS -> listOf(
                expression.left,
                expression.right,
            ).all { it?.isSimpleBooleanExpression() == true }

            in REDUNDANT_IF_COMPARISON_OPERATIONS -> expression.hasSimpleOperandsWithConstant()

            else -> false
        }

        else -> expression.isSimpleOperand() && expression.isNotNullableBoolean()
    }
