package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression

class ConvertArgumentToSet(config: Config) :
    Rule(
        config,
        "A collection argument of `minus`, `intersect`, `subtract`, `removeAll` or `retainAll` " +
            "is searched for every element. Convert the argument to a `Set` with `toSet()`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val argument = analyze(expression) { expression.setConvertibleArgument() } ?: return
        report(Finding(Entity.from(argument), MESSAGE))
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val argument = analyze(expression) { expression.setConvertibleOperand() } ?: return
        report(Finding(Entity.from(argument), MESSAGE))
    }

    private companion object {
        const val MESSAGE = "The argument can be converted to 'Set' to improve performance"
    }
}
