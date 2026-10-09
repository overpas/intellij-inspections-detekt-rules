package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

private val SIMPLIFIABLE_FLOW_CALL_NAMES = setOf("flatMapMerge", "flatMapConcat", "filter")

class SimplifiableFlowCall(config: Config) :
    Rule(
        config,
        "Some `Flow` calls with a trivial lambda have a dedicated operator. " +
            "Replace the call with `flattenMerge`, `flattenConcat`, `filterNotNull` or `filterIsInstance`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        if (callee.text !in SIMPLIFIABLE_FLOW_CALL_NAMES) return
        val replacement = analyze(expression) { SimplifiableFlowCallConversion(this, expression).replacement() }
        if (replacement != null) {
            report(Finding(Entity.from(callee), "'${callee.text}' call could be simplified to '$replacement'"))
        }
    }
}
