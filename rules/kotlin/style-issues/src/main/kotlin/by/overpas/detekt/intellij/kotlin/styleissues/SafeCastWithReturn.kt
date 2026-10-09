package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("SafeCastWithReturn")
class SafeCastWithReturn(config: Config) :
    Rule(
        config,
        "A cast followed by `?: return` only checks the type of a value. " +
            "Use an `if (x !is T) return` type check instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val elvis = SafeCastWithReturnElvis(expression)
        if (elvis.isCandidate() && analyze(expression) { elvis.isStatement() }) {
            report(Finding(Entity.from(expression), "Should be replaced with 'if' type check"))
        }
    }
}
