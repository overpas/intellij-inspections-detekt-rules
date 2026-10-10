package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@IntellijInspection("RedundantInterpolationPrefix")
class RedundantInterpolationPrefix(config: Config) :
    Rule(
        config,
        "An interpolation prefix of a string that has no interpolation or dollar sign that needs it is redundant. " +
            "Remove the prefix.",
    ),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        val prefix = expression.interpolationPrefix ?: return
        val isRedundant = analyze(expression) {
            expression.directDiagnostics(KaDiagnosticCheckerFilter.ONLY_EXPERIMENTAL_CHECKERS)
                .any { it is KaFirDiagnostic.RedundantInterpolationPrefix }
        }
        if (isRedundant) report(Finding(Entity.from(prefix), "Redundant interpolation prefix"))
    }
}
