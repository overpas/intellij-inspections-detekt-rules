package by.overpas.detekt.intellij.kotlin.otherproblems

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtConstantExpression

@IntellijInspection("FloatingPointLiteralPrecision")
class FloatingPointLiteralPrecision(config: Config) :
    Rule(
        config,
        "A floating-point literal has more digits than its type can hold, so the compiler silently rounds it. " +
            "Write the rounded value instead.",
    ),
    RequiresAnalysisApi {

    override fun visitConstantExpression(expression: KtConstantExpression) {
        super.visitConstantExpression(expression)
        if (expression.node.elementType != KtNodeTypes.FLOAT_CONSTANT) return
        val isFloat = analyze(expression) { expression.expressionType?.isFloatType == true }
        if (expression.exceedsFloatingPointPrecision(isFloat)) {
            report(
                Finding(
                    Entity.from(expression),
                    "Floating-point literal cannot be represented with the required precision",
                ),
            )
        }
    }
}
