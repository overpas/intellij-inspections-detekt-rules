package by.overpas.detekt.intellij.kotlin.styleissues.liftreturnorassignment

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtExpression

@IntellijInspection("LiftReturnOrAssignment")
class LiftReturnOrAssignment(config: Config) :
    Rule(
        config,
        "Every branch of this `if`, `when` or `try` ends with a return or with an assignment to the same variable. " +
            "Lift the return or the assignment out of the expression.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Report only the expressions whose branches have the return or the assignment as their only statement.",
    )
    private val isReportedOnlyIfSingleStatement: Boolean by config(true)

    override fun visitExpression(expression: KtExpression) {
        super.visitExpression(expression)
        val keyword = expression.liftReturnOrAssignmentKeyword() ?: return
        val liftType = analyze(expression) {
            expression.liftReturnOrAssignmentType(isReportedOnlyIfSingleStatement)
        } ?: return
        report(Finding(Entity.from(keyword), "'$liftType' can be lifted out of '${keyword.text}'"))
    }
}
