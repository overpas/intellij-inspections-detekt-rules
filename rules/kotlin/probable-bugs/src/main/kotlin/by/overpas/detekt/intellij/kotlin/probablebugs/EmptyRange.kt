package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression

@IntellijInspection("EmptyRange")
class EmptyRange(config: Config) :
    Rule(
        config,
        "A range whose start is past its end in the direction of the operator is empty, so nothing iterates over it. " +
            "Swap the bounds or use the range operator that matches the direction.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        expression.reportIfEmpty()
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        expression.reportIfEmpty()
    }

    private fun KtExpression.reportIfEmpty() {
        EmptyRangeOperator.from(this)?.let { emptyRangeMessage(it) }?.let { report(Finding(Entity.from(this), it)) }
    }
}
