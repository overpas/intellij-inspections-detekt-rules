package by.overpas.detekt.intellij.kotlin.coroutines.coroutinecontextwithjob

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("CoroutineContextWithJob")
class CoroutineContextWithJob(config: Config) :
    Rule(
        config,
        "A `Job` in the context of a coroutine builder replaces the parent job and breaks structured concurrency. " +
            "Remove the `Job` from the context.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Coroutine builders of the project that are checked like `launch`, given as fully qualified names, " +
            "e.g. `com.example.launchSafely`.",
    )
    private val additionalBuilders: Set<String> by config(emptyList<String>()) { it.toSet() }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val problem = analyze(expression) {
            CoroutineContextWithJobBuilderCall(this, expression, additionalBuilders).problem()
        }
        val (source, message) = problem ?: return
        report(Finding(Entity.from(source), message))
    }
}
