package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtParameter

@IntellijInspection("CanBeParameter")
class CanBeParameter(config: Config) :
    Rule(
        config,
        "A constructor property that is only read during initialization does not need to be a property. " +
            "Remove `val` or `var` from the parameter.",
    ),
    RequiresAnalysisApi {

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        val keyword = parameter.valOrVarKeyword ?: return
        val candidate = CanBeParameterCandidate(parameter)
        if (candidate.isConstructorProperty() && candidate.isVisibleOnlyInFile() && candidate.isNeverUsedAsProperty()) {
            report(Finding(Entity.from(keyword), "Constructor parameter is never used as a property"))
        }
    }
}
