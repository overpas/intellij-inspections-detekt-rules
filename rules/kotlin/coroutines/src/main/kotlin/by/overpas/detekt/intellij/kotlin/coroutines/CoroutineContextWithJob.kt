package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class CoroutineContextWithJob(config: Config) :
    Rule(
        config,
        "A `Job` in the context of a coroutine builder replaces the parent job and breaks structured concurrency. " +
            "Remove the `Job` from the context.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val problem = analyze(expression) { CoroutineContextWithJobBuilderCall(this, expression).problem() } ?: return
        report(Finding(Entity.from(problem.source), problem.message))
    }
}
