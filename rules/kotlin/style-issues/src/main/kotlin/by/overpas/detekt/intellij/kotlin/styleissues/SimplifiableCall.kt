package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class SimplifiableCall(config: Config) :
    Rule(
        config,
        "A `flatMap`, `filter` or `mapNotNull` call with a trivial lambda has a dedicated library counterpart. " +
            "Use `flatten`, `filterNotNull` or `filterIsInstance` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val call = SimplifiableCallCandidate(expression)
        val callee = expression.calleeExpression ?: return
        val replacement = if (call.isCandidate()) analyze(expression) { call.replacement() } else null
        if (replacement != null) {
            report(Finding(Entity.from(callee), "'${callee.text}' call could be simplified to '$replacement'"))
        }
    }
}
