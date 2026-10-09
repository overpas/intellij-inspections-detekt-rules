package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

@IntellijInspection("RecursivePropertyAccessor")
class RecursivePropertyAccessor(config: Config) :
    Rule(
        config,
        "A property accessor that reads or writes its own property calls itself endlessly. " +
            "Use the backing field `field` or another property instead.",
    ),
    RequiresAnalysisApi {

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        super.visitSimpleNameExpression(expression)
        val message = expression.recursiveAccessorMessage() ?: return
        report(Finding(Entity.from(expression), message))
    }
}
