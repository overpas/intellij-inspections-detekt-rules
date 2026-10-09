package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression

class ConvertNaNEquality(config: Config) :
    Rule(
        config,
        "An equality check with `NaN` never holds because `NaN` is not equal to any value. Use `isNaN()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.operationToken != KtTokens.EQEQ && expression.operationToken != KtTokens.EXCLEQ) return
        val hasNaNOperand = listOfNotNull(expression.left, expression.right).any { it.isConvertNaNEqualityConstant }
        if (hasNaNOperand) {
            report(
                Finding(Entity.from(expression), "Equality check with NaN should be replaced with 'isNaN()'"),
            )
        }
    }
}
