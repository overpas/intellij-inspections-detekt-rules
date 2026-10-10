package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

private const val SINGLE_MESSAGE = "Redundant SAM constructor"

private const val MULTIPLE_MESSAGE = "Redundant SAM constructors"

@IntellijInspection("RedundantSamConstructor")
class RedundantSamConstructor(config: Config) :
    Rule(
        config,
        "A SAM constructor call passed as an argument is redundant when a lambda converts to the same type. " +
            "Pass the lambda directly.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.valueArguments.all { it.samConstructorCall()?.samLambdaArgument() == null }) return
        val calls = analyze(expression) { expression.convertibleSamCalls() }
        val single = calls.singleOrNull()
        when {
            calls.isEmpty() -> Unit

            single != null -> report(
                Finding(Entity.from(single.calleeExpression ?: single), "Redundant SAM constructor"),
            )

            else -> report(
                Finding(Entity.from(expression.valueArgumentList ?: expression), "Redundant SAM constructors"),
            )
        }
    }
}
