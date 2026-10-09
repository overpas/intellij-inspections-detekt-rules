package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

class FilterIsInstanceResultIsAlwaysEmpty(config: Config) :
    Rule(
        config,
        "A `filterIsInstance` call whose target type no element of the receiver can have always returns an empty " +
            "collection. Fix the target type or remove the call.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name in FILTER_IS_INSTANCE_NAMES && expression.isAlwaysEmptyFilterIsInstance()) {
            report(Finding(Entity.from(expression), "The result of '$name' is always an empty collection"))
        }
    }
}
