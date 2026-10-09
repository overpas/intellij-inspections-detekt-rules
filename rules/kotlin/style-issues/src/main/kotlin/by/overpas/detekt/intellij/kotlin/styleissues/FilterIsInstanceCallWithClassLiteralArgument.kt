package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class FilterIsInstanceCallWithClassLiteralArgument(config: Config) :
    Rule(
        config,
        "`filterIsInstance` with a `Class` literal argument can use a reified type argument. " +
            "Use `filterIsInstance<T>()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val call = FilterIsInstanceCallWithClassLiteralArgumentCall(expression)
        val callee = expression.calleeExpression
        if (callee != null && call.isCandidate() && analyze(expression) { call.isReplaceable() }) {
            report(Finding(Entity.from(callee), "'filterIsInstance' call with a class literal argument"))
        }
    }
}
