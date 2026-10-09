package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

private val equalityTokens = setOf(KtTokens.EQEQ, KtTokens.EXCLEQ)

private val identityTokens = setOf(KtTokens.EQEQEQ, KtTokens.EXCLEQEQEQ)

private val logicalTokens = setOf(KtTokens.ANDAND, KtTokens.OROR)

internal fun KtExpression.equalsCombinationOperands(): List<SuspiciousEqualsCombinationOperand> =
    when {
        this is KtBinaryExpression && operationToken in logicalTokens ->
            listOfNotNull(right, left).flatMap { it.equalsCombinationOperands() }

        this is KtBinaryExpression &&
            operationToken in equalityTokens + identityTokens &&
            listOf(left, right).none { it is KtConstantExpression && it.node.elementType == KtNodeTypes.NULL } ->
            listOfNotNull(left as? KtNameReferenceExpression, right as? KtNameReferenceExpression)
                .map { SuspiciousEqualsCombinationOperand(it.text, operationToken in identityTokens) }

        this is KtParenthesizedExpression -> expression?.equalsCombinationOperands().orEmpty()

        this is KtPrefixExpression && operationToken == KtTokens.EXCL ->
            baseExpression?.equalsCombinationOperands().orEmpty()

        else -> emptyList()
    }
