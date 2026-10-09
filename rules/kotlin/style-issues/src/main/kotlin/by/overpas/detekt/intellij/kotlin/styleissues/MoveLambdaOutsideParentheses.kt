package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("MoveLambdaOutsideParentheses")
class MoveLambdaOutsideParentheses(config: Config) :
    Rule(
        config,
        "A lambda that is passed as the last argument inside the parentheses is harder to read. " +
            "Move the lambda out of the parentheses.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val lambda = expression.movableTrailingLambda() ?: return
        if (analyze(expression) { expression.canMoveLambdaOut() }) {
            report(Finding(Entity.from(lambda), "Lambda argument should be moved out of parentheses"))
        }
    }
}
