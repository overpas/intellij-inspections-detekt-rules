package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("WhenWithOnlyElse")
class WhenWithOnlyElse(config: Config) :
    Rule(
        config,
        "A when with only an else branch always runs that branch. Replace the when with the branch body.",
    ) {

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        if (expression.entries.singleOrNull()?.isElse == true) {
            report(Finding(Entity.from(expression), "'when' has only 'else' branch and should be simplified"))
        }
    }
}
