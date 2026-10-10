package by.overpas.detekt.intellij.kotlin.codemigration

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("ConvertLongToDuration")
class ConvertLongToDuration(config: Config) :
    Rule(
        config,
        "A kotlinx.coroutines function is called with its legacy `Long` milliseconds overload. " +
            "Pass a `Duration` such as `100.milliseconds` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.takeIf { it.text in CONVERT_LONG_TO_DURATION_FUNCTIONS } ?: return
        val isPositional = expression.valueArguments.firstOrNull()?.isNamed() == false
        if (isPositional && analyze(expression) { expression.isConvertLongToDurationCall() }) {
            report(Finding(Entity.from(callee), "Legacy Long overload can be converted to Duration"))
        }
    }
}
