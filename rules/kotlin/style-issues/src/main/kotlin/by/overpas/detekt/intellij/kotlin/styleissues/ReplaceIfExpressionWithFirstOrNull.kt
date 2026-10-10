package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtIfExpression

@IntellijInspection("ReplaceIfExpressionWithFirstOrNull")
class ReplaceIfExpressionWithFirstOrNull(config: Config) :
    Rule(
        config,
        "An `if` expression that returns the first element of a non-empty collection, else `null`, is verbose. " +
            "Replace it with `firstOrNull()`.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        if (ReplaceIfExpressionWithFirstOrNullCandidate(expression).isReplaceable()) {
            report(Finding(Entity.from(expression.ifKeyword), "'if' expression can be replaced with 'firstOrNull()'"))
        }
    }
}
