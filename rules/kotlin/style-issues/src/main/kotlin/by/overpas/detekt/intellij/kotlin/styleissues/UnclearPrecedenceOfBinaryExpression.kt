package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression

class UnclearPrecedenceOfBinaryExpression(config: Config) :
    Rule(
        config,
        "An expression mixes operators whose precedence is easy to misread, such as `?:` with `==` or `+`. " +
            "Add clarifying parentheses.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        reportIfUnclear(expression)
    }

    override fun visitBinaryWithTypeRHSExpression(expression: KtBinaryExpressionWithTypeRHS) {
        super.visitBinaryWithTypeRHSExpression(expression)
        reportIfUnclear(expression)
    }

    override fun visitIsExpression(expression: KtIsExpression) {
        super.visitIsExpression(expression)
        reportIfUnclear(expression)
    }

    private fun reportIfUnclear(expression: KtExpression) {
        val node = expression.unclearPrecedenceNode() ?: return
        if (node.isRoot() && node.isUnclear()) {
            report(Finding(Entity.from(node.expression), "Expression should use clarifying parentheses"))
        }
    }
}
