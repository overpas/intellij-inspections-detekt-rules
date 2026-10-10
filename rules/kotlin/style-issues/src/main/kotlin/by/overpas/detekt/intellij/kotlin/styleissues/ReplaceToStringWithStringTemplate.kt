package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

@IntellijInspection("ReplaceToStringWithStringTemplate")
class ReplaceToStringWithStringTemplate(config: Config) :
    Rule(
        config,
        "A `toString()` call on a reference can be written as a string template. Use a string template instead.",
    ),
    RequiresAnalysisApi {

    @Configuration("The minimum number of `toString()` calls in one string concatenation that the rule reports")
    private val minInterpolatedValues: Int by config(1)

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val isReported = ReplaceToStringWithStringTemplateCall(expression).isConversion() &&
            ReplaceToStringWithStringTemplateConcatenation(expression).conversionCount() >= minInterpolatedValues
        if (isReported) {
            report(Finding(Entity.from(expression), "Call of 'toString' could be replaced with string template"))
        }
    }
}
