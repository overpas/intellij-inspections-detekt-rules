package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("ConvertRangeCheckToTwoComparisons")
class ConvertRangeCheckToTwoComparisons(config: Config) :
    Rule(
        config,
        "A range check of simple values can be written as two comparisons. " +
            "Replace `x in a..b` with `a <= x && x <= b`.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operands = expression.rangeCheckOperands() ?: return
        val range = expression.right ?: return
        val isConvertible = analyze(expression) {
            val type = operands.first().expressionType
            val callableId = range.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
            type != null &&
                operands.all { it.expressionType?.semanticallyEquals(type) == true } &&
                callableId?.run { asSingleFqName().asString().startsWith("kotlin.") } == true
        }
        if (isConvertible) report(Finding(Entity.from(expression), "Range check can be converted to comparisons"))
    }
}
