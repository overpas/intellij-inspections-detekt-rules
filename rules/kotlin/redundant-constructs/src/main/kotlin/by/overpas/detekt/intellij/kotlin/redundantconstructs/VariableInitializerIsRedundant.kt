package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.psi.KtFile

@IntellijInspection("VariableInitializerIsRedundant")
class VariableInitializerIsRedundant(config: Config) :
    Rule(
        config,
        "The initial value of a variable is overwritten before it is read. Remove the initializer.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val elements = analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS)
                .filter { it.factoryName == DIAGNOSTIC }
                .map { it.psi }
        }
        elements.forEach { report(Finding(Entity.from(it), "Initializer is redundant")) }
    }

    private companion object {
        const val DIAGNOSTIC = "VARIABLE_INITIALIZER_IS_REDUNDANT"
    }
}
