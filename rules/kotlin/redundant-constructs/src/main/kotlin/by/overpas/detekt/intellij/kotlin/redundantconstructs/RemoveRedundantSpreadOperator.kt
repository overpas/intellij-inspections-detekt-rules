package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtValueArgument

@IntellijInspection("RemoveRedundantSpreadOperator")
class RemoveRedundantSpreadOperator(config: Config) :
    Rule(
        config,
        "A spread operator on an array that is created in place is redundant. Pass the elements directly.",
    ),
    RequiresAnalysisApi {

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        if (argument.getSpreadElement() == null || argument.isNamed()) return
        if (RemoveRedundantSpreadOperatorArgument(argument).isRedundant()) {
            report(Finding(Entity.from(argument), "Redundant spread operator"))
        }
    }
}
