package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression

internal class UseExpressionBodyBranches(expression: KtExpression) {

    private val results = generateSequence(listOf(expression)) { level ->
        level.flatMap { it.resultBranches() }.takeIf { it.isNotEmpty() }
    }.flatten().toList()

    @OptIn(KaIdeApi::class)
    context(session: KaSession)
    fun areExhaustive(): Boolean =
        results.none { it is KtIfExpression && it.`else` == null } &&
            results.filterIsInstance<KtWhenExpression>().all {
                it.elseExpression != null || with(session) { it.computeMissingCases() }.isEmpty()
            }

    private companion object {

        fun KtExpression.resultBranches(): List<KtExpression> =
            when (this) {
                is KtIfExpression -> listOfNotNull(then, `else`)
                is KtWhenExpression -> entries.mapNotNull { it.expression }
                is KtBinaryExpression -> listOfNotNull(left, right)
                is KtUnaryExpression -> listOfNotNull(baseExpression)
                is KtBlockExpression -> listOfNotNull(statements.lastOrNull())
                else -> emptyList()
            }
    }
}
