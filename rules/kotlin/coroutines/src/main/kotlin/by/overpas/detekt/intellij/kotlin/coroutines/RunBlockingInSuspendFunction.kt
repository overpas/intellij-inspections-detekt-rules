package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class RunBlockingInSuspendFunction(config: Config) :
    Rule(
        config,
        "`runBlocking` inside a suspend function or a suspend lambda blocks the calling thread. " +
            "Call the code directly, or use `withContext` or `coroutineScope`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isReported = expression.calleeExpression?.text == "runBlocking" &&
            expression.lambdaArguments.size == 1 &&
            expression.valueArguments.size in 1..2 &&
            analyze(expression) {
                RunBlockingInSuspendFunctionCall(this, expression).isRunBlocking() &&
                    RunBlockingInSuspendFunctionContext(this, expression).isSuspend()
            }
        if (isReported) {
            report(
                Finding(
                    Entity.from(expression.calleeExpression ?: expression),
                    "Using 'runBlocking' inside a suspend function blocks the calling thread " +
                        "and defeats the purpose of asynchronous programming",
                ),
            )
        }
    }
}
