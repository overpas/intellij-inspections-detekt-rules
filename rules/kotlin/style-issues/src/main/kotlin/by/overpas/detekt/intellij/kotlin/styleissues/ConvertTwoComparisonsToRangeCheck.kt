package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("ConvertTwoComparisonsToRangeCheck")
class ConvertTwoComparisonsToRangeCheck(config: Config) :
    Rule(
        config,
        "Two comparisons of one value with a lower and an upper bound are harder to read than a range check. " +
            "Use `in` or `!in` with a range instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val bounds = ConvertTwoComparisonsToRangeCheckPair(expression).bounds ?: return
        val range = ConvertTwoComparisonsToRangeCheckTypes(expression, bounds).rangeText ?: return
        if (!ConvertTwoComparisonsToRangeCheckContains(bounds.value, range).isRecursive) {
            report(Finding(Entity.from(expression), "Two comparisons should be converted to a range check"))
        }
    }
}
