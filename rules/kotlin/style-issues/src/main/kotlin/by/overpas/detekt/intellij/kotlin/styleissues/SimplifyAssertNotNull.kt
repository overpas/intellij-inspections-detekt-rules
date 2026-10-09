package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class SimplifyAssertNotNull(config: Config) :
    Rule(
        config,
        "An `assert` that only checks the variable declared right before it for `null` can be folded into the " +
            "initializer. Use `!!` or `?: error(...)` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val assertCall = SimplifyAssertNotNullCall(expression)
        val callee = expression.calleeExpression
        if (callee != null && assertCall.isCandidate() && analyze(expression) { assertCall.isKotlinAssert() }) {
            report(Finding(Entity.from(callee), "assert should be replaced with operator"))
        }
    }
}
