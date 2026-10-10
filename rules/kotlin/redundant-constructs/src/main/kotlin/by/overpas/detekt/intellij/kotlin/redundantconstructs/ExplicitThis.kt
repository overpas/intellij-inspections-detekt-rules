package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtThisExpression

@IntellijInspection("ExplicitThis")
class ExplicitThis(config: Config) :
    Rule(
        config,
        "An explicit `this` receiver that the call resolves to anyway is redundant. Remove the `this` receiver.",
    ),
    RequiresAnalysisApi {

    override fun visitThisExpression(expression: KtThisExpression) {
        super.visitThisExpression(expression)
        if (ExplicitThisCandidate(expression).isRedundant()) {
            report(Finding(Entity.from(expression), "Redundant explicit this"))
        }
    }
}
