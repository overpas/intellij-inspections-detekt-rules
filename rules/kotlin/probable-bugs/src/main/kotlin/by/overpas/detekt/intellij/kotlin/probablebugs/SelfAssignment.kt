package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression

class SelfAssignment(config: Config) :
    Rule(
        config,
        "Assigning a variable to itself has no effect. Remove the assignment.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val left = expression.left?.selfAssignmentReference()
        val right = expression.right?.selfAssignmentReference()
        if (expression.operationToken != KtTokens.EQ || left == null || left.text != right?.text) return
        val name = analyze(expression) { expression.selfAssignmentVariableName() }
        if (name != null) report(Finding(Entity.from(expression), "Variable '$name' is assigned to itself"))
    }
}
