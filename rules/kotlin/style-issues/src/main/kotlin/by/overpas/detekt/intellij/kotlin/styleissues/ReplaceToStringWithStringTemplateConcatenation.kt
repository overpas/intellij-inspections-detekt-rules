package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression

internal class ReplaceToStringWithStringTemplateConcatenation(expression: KtExpression) {

    private val root = generateSequence(expression) { child ->
        (child.parent as? KtExpression)?.takeIf { parent ->
            parent is KtParenthesizedExpression ||
                (parent is KtBinaryExpression && parent.operationToken == KtTokens.PLUS)
        }
    }.last()

    fun conversionCount(): Int =
        root.operands().count { operand ->
            operand is KtDotQualifiedExpression && ReplaceToStringWithStringTemplateCall(operand).isConversion()
        }

    private companion object {

        fun KtExpression.operands(): List<KtExpression> =
            when (this) {
                is KtParenthesizedExpression -> expression?.operands().orEmpty()

                is KtBinaryExpression if operationToken == KtTokens.PLUS -> listOfNotNull(
                    left,
                    right,
                ).flatMap { it.operands() }

                else -> listOf(this)
            }
    }
}
