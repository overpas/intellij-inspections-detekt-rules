package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtIfExpression

@IntellijInspection("SuspiciousCascadingIf")
class SuspiciousCascadingIf(config: Config) :
    Rule(
        config,
        "A call or an operator after the last 'else' block of a cascading 'if' applies only to the nested 'if'. " +
            "Add braces around the nested 'if' or replace the cascade with 'when'.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        if (expression.condition == null || expression.then == null) return
        if (expression.parent.node.elementType == KtNodeTypes.ELSE) return
        if (expression.suspiciousCascadingIfOperand() is KtIfExpression) {
            report(Finding(Entity.from(expression.ifKeyword), "Suspicious cascading 'if' expression"))
        }
    }
}
