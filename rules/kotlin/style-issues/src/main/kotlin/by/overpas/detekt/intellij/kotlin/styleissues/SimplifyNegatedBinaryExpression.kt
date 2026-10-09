package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtPrefixExpression

class SimplifyNegatedBinaryExpression(config: Config) :
    Rule(
        config,
        "A negated comparison, type check, `in` check or boolean literal is harder to read. " +
            "Use the inverted operator or literal instead.",
    ),
    RequiresAnalysisApi {

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        if (expression.isSimplifiableNegation()) {
            report(Finding(Entity.from(expression), "Negated operation can be simplified"))
        }
    }
}
