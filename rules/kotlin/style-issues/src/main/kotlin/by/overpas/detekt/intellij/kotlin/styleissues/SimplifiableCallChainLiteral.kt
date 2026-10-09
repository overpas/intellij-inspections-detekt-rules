package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

internal class SimplifiableCallChainLiteral(root: KtExpression?) {

    private val results = generateSequence(listOf(root)) { level ->
        level.flatMap { result ->
            when (val expression = result?.let { KtPsiUtil.safeDeparenthesize(it) }) {
                is KtBinaryExpression -> listOf(expression.left, expression.right)

                is KtIfExpression ->
                    listOf(expression.then?.lastBlockStatementOrThis(), expression.`else`?.lastBlockStatementOrThis())

                is KtWhenExpression -> expression.entries.map { it.expression?.lastBlockStatementOrThis() }

                is KtTryExpression ->
                    listOf(expression.tryBlock.lastBlockStatementOrThis()) +
                        expression.catchClauses.map { it.catchBody?.lastBlockStatementOrThis() }

                else -> emptyList()
            }
        }.ifEmpty { null }
    }.flatten()

    fun isLiteral(): Boolean =
        results.all { result ->
            when (result?.let { KtPsiUtil.safeDeparenthesize(it) }) {
                is KtBinaryExpression,
                is KtIfExpression,
                is KtWhenExpression,
                is KtTryExpression,
                is KtConstantExpression,
                -> true

                else -> false
            }
        }
}
