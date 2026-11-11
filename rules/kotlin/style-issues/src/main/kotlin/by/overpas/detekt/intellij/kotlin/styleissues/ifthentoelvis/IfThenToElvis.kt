package by.overpas.detekt.intellij.kotlin.styleissues.ifthentoelvis

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.kotlin.styleissues.IfThenToSafeAccessStrategy
import by.overpas.detekt.intellij.kotlin.styleissues.ifThenSmartCastStability
import by.overpas.detekt.intellij.kotlin.styleissues.ifThenToSafeAccessData
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtIfExpression

@IntellijInspection("IfThenToElvis")
class IfThenToElvis(config: Config) :
    Rule(
        config,
        "An `if` expression checks a value for `null` or for a type and has a fallback branch. " +
            "Replace it with the elvis operator `?:`.",
    ),
    RequiresAnalysisApi {

    @Configuration("Also report the `if` statements whose value is not used.")
    private val areStatementsReported: Boolean by config(false)

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val data = expression.ifThenToSafeAccessData() ?: return
        val isFoldable = analyze(expression) {
            val clauses = listOfNotNull(data.checkedExpression, data.baseClause, data.negatedClause)
            val isReportedUsage = areStatementsReported ||
                (expression.isUsedAsExpression && expression.expressionType?.isUnitType != true)
            isReportedUsage &&
                IfThenToSafeAccessStrategy(this, data).isSuggested() == true &&
                IfThenToElvisClauses(this, data).areReplaceable() &&
                clauses.none { it.ifThenSmartCastStability() == false }
        }
        if (isFoldable) report(Finding(Entity.from(expression.ifKeyword), "If-Then foldable to '?:'"))
    }
}
