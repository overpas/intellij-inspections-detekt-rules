package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtIfExpression

class RedundantIf(config: Config) :
    Rule(
        config,
        "An 'if' that only chooses between `true` and `false` or a boolean expression is redundant. " +
            "Replace it with the condition.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        if (RedundantIfCandidate(expression).isRedundant()) {
            report(Finding(Entity.from(expression.ifKeyword), "Redundant 'if' statement"))
        }
    }
}
