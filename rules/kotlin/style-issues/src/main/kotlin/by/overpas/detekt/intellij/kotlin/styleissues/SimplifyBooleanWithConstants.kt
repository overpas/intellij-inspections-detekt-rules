package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

class SimplifyBooleanWithConstants(config: Config) :
    Rule(
        config,
        "This boolean expression contains constant operands that make parts of it redundant. " +
            "Simplify the expression.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.parent is KtBinaryExpression) return
        if (analyze(expression) { expression.isSimplifiableBooleanRoot() }) {
            report(Finding(Entity.from(expression), "Boolean expression can be simplified"))
        }
    }
}
