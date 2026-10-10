package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("SuspiciousEqualsCombination")
class SuspiciousEqualsCombination(config: Config) :
    Rule(
        config,
        "A condition compares the same variable with both '==' and '==='. " +
            "Use one kind of equality for the variable.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.parent is KtBinaryExpression) return
        val (identityOperands, equalityOperands) = expression.equalsCombinationOperands()
            .partition { it.isIdentity }
            .toList()
            .map { operands -> operands.map { it.name }.toSet() }
        if (identityOperands.intersect(equalityOperands).isNotEmpty()) {
            report(Finding(Entity.from(expression), "Suspicious combination of == and ==="))
        }
    }
}
