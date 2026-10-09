package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtIfExpression

class RedundantElseInIf(config: Config) :
    Rule(
        config,
        "An `else` branch is redundant when every preceding `if` branch ends with a jump such as `return` or " +
            "`throw`. Remove the `else` and move its body after the `if`.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val chain = RedundantElseInIfChain(expression)
        if (chain.isElseBranch) return
        val elseKeyword = chain.lastElseKeyword ?: return
        if (analyze(expression) { chain.isRedundant() }) {
            report(Finding(Entity.from(elseKeyword), "Redundant 'else'"))
        }
    }
}
