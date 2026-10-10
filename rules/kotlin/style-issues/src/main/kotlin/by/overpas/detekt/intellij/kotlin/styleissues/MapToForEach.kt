package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("MapToForEach")
class MapToForEach(config: Config) :
    Rule(
        config,
        "A `map`-like call whose result is not used only runs its lambda for side effects. " +
            "Replace it with `forEach` or `forEachIndexed`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.takeIf { expression.isMapToForEachCandidate() } ?: return
        if (analyze(expression) { expression.isMapToForEachReplaceable() }) {
            report(Finding(Entity.from(callee), "'${callee.text}' call can be replaced with 'forEach'"))
        }
    }
}
