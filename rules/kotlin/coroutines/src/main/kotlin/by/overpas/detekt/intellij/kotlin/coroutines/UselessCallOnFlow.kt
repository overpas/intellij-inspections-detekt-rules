package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

class UselessCallOnFlow(config: Config) :
    Rule(
        config,
        "A `filterNotNull`, `filterIsInstance` or `mapNotNull` call on a `Flow` that cannot hold the filtered " +
            "values does nothing. Remove the call or replace `mapNotNull` with `map`.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val call = expression.selectorExpression as? KtCallExpression
        val callee = call?.calleeExpression ?: return
        val message = MESSAGES[callee.text] ?: return
        val isReported = analyze(expression) {
            val flowCall = UselessCallOnFlowCall(this, expression.receiverExpression, call)
            when (callee.text) {
                FILTER_NOT_NULL -> flowCall.isRedundantNullFilter()
                FILTER_IS_INSTANCE -> flowCall.isRedundantInstanceFilter()
                else -> flowCall.isReducibleMapNotNull()
            }
        }
        if (isReported) report(Finding(Entity.from(callee), message))
    }

    private companion object {
        const val FILTER_NOT_NULL = "filterNotNull"
        const val FILTER_IS_INSTANCE = "filterIsInstance"
        const val REDUNDANT_CALL = "Redundant call on 'Flow' type"
        val MESSAGES = mapOf(
            FILTER_NOT_NULL to REDUNDANT_CALL,
            FILTER_IS_INSTANCE to REDUNDANT_CALL,
            "mapNotNull" to "Call on 'Flow' type may be reduced",
        )
    }
}
