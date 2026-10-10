package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("SuspiciousVarProperty")
class SuspiciousVarProperty(config: Config) :
    Rule(
        config,
        "The getter of this `var` property does not read its backing field, " +
            "so assignments have no visible effect. Change it to a `val` without an initializer.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val keyword = property.valOrVarKeyword
        val isSuspicious = property.isSuspiciousVarCandidate() &&
            analyze(property) { property.isSuspiciousVarProperty() }
        if (isSuspicious) report(Finding(Entity.from(keyword), MESSAGE))
    }

    private companion object {
        const val MESSAGE =
            "Suspicious 'var' property: its setter does not influence its getter result"
    }
}
