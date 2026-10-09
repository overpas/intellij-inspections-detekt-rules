package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("ReplaceWithCallWithContextCall")
class ReplaceWithCallWithContextCall(config: Config) :
    Rule(
        config,
        "A `with` call whose receiver is only passed on as a context argument hides its intent. " +
            "Use a `context` call instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val lambda = ReplaceWithCallWithContextCallShape(expression).lambda ?: return
        val isContextOnly = analyze(expression) {
            ReplaceWithCallWithContextCallUsages(this, expression, lambda).usesReceiverOnlyAsContext()
        }
        if (isContextOnly) {
            report(Finding(Entity.from(expression.calleeExpression ?: expression), "Replace 'with' with 'context'"))
        }
    }
}
