package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("VerboseNullabilityAndEmptiness")
class VerboseNullabilityAndEmptiness(config: Config) :
    Rule(
        config,
        "A null check followed by an emptiness or blankness check of the same value, such as " +
            "`x == null || x.isEmpty()`, is verbose. Use `isNullOrEmpty()` or `isNullOrBlank()`.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val candidate = expression.verboseNullabilityCandidate() ?: return
        if (analyze(expression) { candidate.isConfirmed() }) {
            report(Finding(Entity.from(expression), "Replace subsequent checks with '${candidate.replacement}()' call"))
        }
    }
}
