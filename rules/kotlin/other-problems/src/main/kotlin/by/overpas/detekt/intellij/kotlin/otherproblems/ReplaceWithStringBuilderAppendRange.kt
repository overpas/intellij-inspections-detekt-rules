package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceWithStringBuilderAppendRange(config: Config) :
    Rule(
        config,
        "On the JVM, `append(CharArray, offset, len)` takes a length, unlike the common `appendRange` " +
            "that takes start and end indices. Use `appendRange` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "append" || expression.valueArguments.size != 3) return
        if (analyze(expression) { expression.appendsCharArrayRange() }) {
            report(Finding(Entity.from(expression.calleeExpression ?: expression), "Replace with 'appendRange'"))
        }
    }
}
