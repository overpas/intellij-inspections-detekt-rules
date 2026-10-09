package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("ReplaceWithOperatorAssignment")
class ReplaceWithOperatorAssignment(config: Config) :
    Rule(
        config,
        "An assignment of an arithmetic operation on the assigned variable can be shorter. " +
            "Use an operator assignment such as `+=` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val candidate = ReplaceWithOperatorAssignmentCandidate(expression)
        if (!candidate.isCandidate() || !analyze(expression) { candidate.isRepeated() }) return
        val replacement = candidate.replacement() ?: return
        if (analyze(replacement) { candidate.resolves(replacement) }) {
            report(Finding(Entity.from(expression), "Replaceable with operator-assignment"))
        }
    }
}
