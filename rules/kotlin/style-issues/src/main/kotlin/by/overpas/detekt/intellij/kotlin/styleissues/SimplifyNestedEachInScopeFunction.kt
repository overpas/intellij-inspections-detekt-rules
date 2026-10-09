package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class SimplifyNestedEachInScopeFunction(config: Config) :
    Rule(
        config,
        "An `also` or `apply` call that only iterates its receiver with `forEach` or `onEach` is verbose. " +
            "Call `onEach` on the receiver instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val nested = expression.nestedEachInScopeFunction() ?: return
        val message = analyze(expression) { nested.message() } ?: return
        report(Finding(Entity.from(expression.calleeExpression ?: expression), message))
    }
}
