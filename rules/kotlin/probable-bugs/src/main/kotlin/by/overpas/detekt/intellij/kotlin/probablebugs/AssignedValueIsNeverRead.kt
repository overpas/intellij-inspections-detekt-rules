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

@IntellijInspection("AssignedValueIsNeverRead")
class AssignedValueIsNeverRead(config: Config) :
    Rule(
        config,
        "A value that is assigned to a variable and never read is useless. Remove the assignment or use the value.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS)
                .filterIsInstance<KaFirDiagnostic.AssignedValueIsNeverRead>()
                .map { it.psi }
        }.forEach { report(Finding(Entity.from(it), "Assigned value is never read")) }
    }
}
