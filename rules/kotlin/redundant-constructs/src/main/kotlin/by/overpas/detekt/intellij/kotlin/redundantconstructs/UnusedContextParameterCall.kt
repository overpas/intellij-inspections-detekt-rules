package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

private const val CONTEXT = "context"

class UnusedContextParameterCall(config: Config) :
    Rule(
        config,
        "A `context` call provides context arguments that its block never uses. Remove the unused arguments " +
            "or the whole call.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val lambda = expression.contextLambda() ?: return
        val callee = expression.calleeExpression
        if (callee?.text != CONTEXT) return
        val hasUnused = analyze(expression) {
            val arguments = UnusedContextParameterCallArguments(this, expression, lambda)
            arguments.targetsKotlinContext() &&
                arguments.matchParameters() &&
                arguments.areSideEffectFree() &&
                arguments.hasUnused()
        }
        if (hasUnused) report(Finding(Entity.from(callee), "Unused context call arguments"))
    }

    private fun KtCallExpression.contextLambda(): KtLambdaExpression? =
        lambdaArguments.lastOrNull()?.getLambdaExpression()
            ?: valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
}
