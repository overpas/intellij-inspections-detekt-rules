package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

private val THROWABLE_NAME_HINTS = listOf("Exception", "Error")

@IntellijInspection("KotlinThrowableNotThrown")
class KotlinThrowableNotThrown(config: Config) :
    Rule(
        config,
        "A call creates or returns a `Throwable` that is never thrown, returned or stored. " +
            "Throw the `Throwable` or remove the call.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val message = if (THROWABLE_NAME_HINTS.any { it in callee.text }) {
            analyze(expression) { expression.notThrownThrowableMessage() }
        } else {
            null
        }
        if (message != null) report(Finding(Entity.from(callee), message))
    }
}
