package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

@IntellijInspection("ScopeFunctionConversion")
class ScopeFunctionConversion(config: Config) :
    Rule(
        config,
        "A scope function call can be replaced with another scope function. Choose the one that reads best.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = (expression.calleeExpression as? KtNameReferenceExpression)
            ?.takeIf { it.getReferencedName() in COUNTERPARTS }
        val lambda = expression.lambdaArguments.singleOrNull()?.getLambdaExpression()
        if (callee == null || lambda == null) return
        val calleeName = callee.getReferencedName()
        val hasSimpleLambda = lambda.valueParameters.isEmpty() || lambda.isSimpleScopeFunctionLambda()
        val isConvertible = hasSimpleLambda &&
            callee.isScopeFunctionCallee() &&
            COUNTERPARTS.getValue(calleeName).any { expression.isValidScopeCounterpart(calleeName, it) }
        if (isConvertible) {
            report(Finding(Entity.from(callee), "Call can be replaced with another scope function"))
        }
    }

    private companion object {
        val COUNTERPARTS = mapOf(
            "apply" to listOf("also"),
            "run" to listOf(SCOPE_FUNCTION_WITH, "let"),
            "also" to listOf("apply"),
            "let" to listOf("run", SCOPE_FUNCTION_WITH),
            SCOPE_FUNCTION_WITH to listOf("run", "let"),
        )
    }
}
