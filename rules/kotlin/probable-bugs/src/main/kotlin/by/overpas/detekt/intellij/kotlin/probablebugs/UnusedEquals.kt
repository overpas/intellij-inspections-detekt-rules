package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("UnusedEquals")
class UnusedEquals(config: Config) :
    Rule(
        config,
        "An equality check whose result is not used has no effect. " +
            "Use the result or remove the expression.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.isUnusedEqualsCandidate() && expression.isUnusedEqualsResult()) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        expression.unusedEqualsCallTarget()
            ?.takeIf { it.isUnusedEqualsResult() }
            ?.let { report(Finding(Entity.from(it), MESSAGE)) }
    }

    private companion object {
        const val MESSAGE = "Unused equals expression"
    }
}
