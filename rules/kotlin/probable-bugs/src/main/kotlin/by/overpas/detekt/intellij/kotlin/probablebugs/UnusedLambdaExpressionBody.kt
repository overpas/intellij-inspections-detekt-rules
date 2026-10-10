package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaExpression

@IntellijInspection("UnusedLambdaExpressionBody")
class UnusedLambdaExpressionBody(config: Config) :
    Rule(
        config,
        "A call to a function with a lambda expression body only creates the lambda and does not run it. Give the " +
            "function a block body or invoke the result.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isCallee = (expression.parent as? KtCallExpression)?.calleeExpression == expression
        if (!isCallee && expression.returnsUnusedLambda()) {
            report(
                Finding(
                    Entity.from(expression.calleeExpression ?: expression),
                    "Unused return value of a function with lambda expression body",
                ),
            )
        }
    }

    private fun KtCallExpression.returnsUnusedLambda(): Boolean {
        val call = this
        return analyze(call) {
            val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val function = (symbol?.psi as? KtFunction)?.takeUnless { it.hasBlockBody() }
            val isUnused = !call.isUsedAsExpression
            isUnused && symbol?.returnType is KaFunctionType && function?.bodyExpression is KtLambdaExpression
        }
    }
}
