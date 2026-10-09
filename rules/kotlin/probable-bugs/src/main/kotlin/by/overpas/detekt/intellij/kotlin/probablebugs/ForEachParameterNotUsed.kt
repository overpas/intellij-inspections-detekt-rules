package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("ForEachParameterNotUsed")
class ForEachParameterNotUsed(config: Config) :
    Rule(
        config,
        "A `forEach` lambda that ignores its implicit parameter only repeats its body. " +
            "Use `repeat` or name the parameter `_` to show the intent.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        expression.unusedForEachParameterName()?.let {
            report(Finding(Entity.from(expression), "Loop parameter '$it' is unused"))
        }
    }
}
