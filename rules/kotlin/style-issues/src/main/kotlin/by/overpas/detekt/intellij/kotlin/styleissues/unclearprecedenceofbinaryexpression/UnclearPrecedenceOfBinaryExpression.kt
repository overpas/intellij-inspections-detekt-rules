package by.overpas.detekt.intellij.kotlin.styleissues.unclearprecedenceofbinaryexpression

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression

@IntellijInspection("UnclearPrecedenceOfBinaryExpression")
class UnclearPrecedenceOfBinaryExpression(config: Config) :
    Rule(
        config,
        "An expression mixes operators whose precedence is easy to misread, such as `?:` with `==` or `+`. " +
            "Add clarifying parentheses.",
    ),
    RequiresAnalysisApi {

    @Configuration("Also report the operators of different kinds without parentheses whose precedence is well known.")
    private val areObviousCasesReported: Boolean by config(false)

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
        if (node.isRoot() && node.isUnclear(areObviousCasesReported)) {
            report(Finding(Entity.from(node.expression), "Expression should use clarifying parentheses"))
        }
    }
}
