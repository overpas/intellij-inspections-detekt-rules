package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile

class RedundantModalityModifier(config: Config) :
    Rule(
        config,
        "A modality modifier that matches the default modality of the declaration is redundant. Remove it.",
    ),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val owners = analyze(file) {
            file.collectDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXTENDED_CHECKERS)
                .filterIsInstance<KaFirDiagnostic.RedundantModalityModifier>()
                .map { it.psi }
        }
        owners.mapNotNull { it.modifierList?.getModifier(KtTokens.MODALITY_MODIFIERS) }
            .forEach { report(Finding(Entity.from(it), "Redundant '${it.text}' modifier")) }
    }
}
