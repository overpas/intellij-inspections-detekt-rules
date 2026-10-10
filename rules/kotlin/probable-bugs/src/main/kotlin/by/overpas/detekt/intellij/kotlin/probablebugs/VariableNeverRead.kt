package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.psi.KtFile

@IntellijInspection("VariableNeverRead")
class VariableNeverRead(config: Config) :
    Rule(
        config,
        "A variable that is only written and never read has no effect. Remove the variable or read its value.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS)
                .filterIsInstance<KaFirDiagnostic.VariableNeverRead>()
                .map { it.psi }
        }.forEach { report(Finding(Entity.atName(it), "Variable '${it.nameAsSafeName.asString()}' is never read")) }
    }
}
