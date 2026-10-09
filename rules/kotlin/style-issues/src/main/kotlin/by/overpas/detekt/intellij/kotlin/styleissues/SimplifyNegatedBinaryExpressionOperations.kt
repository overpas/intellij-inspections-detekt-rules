package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression

private val simplifyNegatedBinaryExpressionOrderings = setOf(KtTokens.LT, KtTokens.LTEQ, KtTokens.GT, KtTokens.GTEQ)

private val simplifyNegatedBinaryExpressionInvertibles =
    simplifyNegatedBinaryExpressionOrderings +
        setOf(
            KtTokens.IN_KEYWORD,
            KtTokens.NOT_IN,
            KtTokens.EQEQ,
            KtTokens.EXCLEQ,
            KtTokens.EQEQEQ,
            KtTokens.EXCLEQEQEQ,
        )

private val simplifyNegatedBinaryExpressionFloatingPoints = setOf(StandardClassIds.Float, StandardClassIds.Double)

internal fun KtExpression?.isInvertibleOperation(): Boolean =
    when (this) {
        is KtIsExpression -> typeReference != null

        is KtBinaryExpression ->
            left != null && right != null && operationToken in simplifyNegatedBinaryExpressionInvertibles &&
                keepsSemanticsWhenInverted()

        else -> false
    }

private fun KtBinaryExpression.keepsSemanticsWhenInverted(): Boolean =
    operationToken !in simplifyNegatedBinaryExpressionOrderings ||
        analyze(this) {
            listOfNotNull(left, right).none { operand ->
                (operand.expressionType as? KaClassType)?.classId in simplifyNegatedBinaryExpressionFloatingPoints
            }
        }
