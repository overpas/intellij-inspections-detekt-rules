package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("ReplaceWithIgnoreCaseEquals")
class ReplaceWithIgnoreCaseEquals(config: Config) :
    Rule(
        config,
        "Comparing two strings after the same case conversion creates new strings. " +
            "Use `equals(..., ignoreCase = true)` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operands = ReplaceWithIgnoreCaseEqualsOperands(expression)
        if (operands.isCandidate() && operands.haveSameConversion()) {
            report(Finding(Entity.from(expression), "Should be replaced with 'equals(..., ignoreCase = true)'"))
        }
    }
}
