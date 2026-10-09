package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.psi.KtFile

class UnusedLambdaExpression(config: Config) :
    Rule(
        config,
        "A lambda expression that is neither called nor stored is never executed. Call it, remove it or use " +
            "`run { ... }`.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS)
                .filterIsInstance<KaFirDiagnostic.UnusedLambdaExpression>()
                .map { it.psi }
        }.forEach { report(Finding(Entity.from(it), "Lambda expression is never invoked")) }
    }
}
