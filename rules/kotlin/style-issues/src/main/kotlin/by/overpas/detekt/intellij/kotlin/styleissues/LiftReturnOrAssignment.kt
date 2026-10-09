package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtExpression

class LiftReturnOrAssignment(config: Config) :
    Rule(
        config,
        "Every branch of this `if`, `when` or `try` ends with a return or with an assignment to the same variable. " +
            "Lift the return or the assignment out of the expression.",
    ),
    RequiresAnalysisApi {

    override fun visitExpression(expression: KtExpression) {
        super.visitExpression(expression)
        val keyword = expression.liftReturnOrAssignmentKeyword() ?: return
        val liftType = analyze(expression) { expression.liftReturnOrAssignmentType() } ?: return
        report(Finding(Entity.from(keyword), "'$liftType' can be lifted out of '${keyword.text}'"))
    }
}
