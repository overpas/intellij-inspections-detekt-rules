package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("JavaMapForEach")
class JavaMapForEach(config: Config) :
    Rule(
        config,
        "Java's `Map.forEach` is called with a two-parameter lambda. " +
            "Use Kotlin's `forEach` with a destructured entry `{ (key, value) -> ... }` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val isJavaMapForEach = expression.hasJavaMapForEachShape() &&
            analyze(expression) { expression.isJavaMapForEachCall() }
        if (isJavaMapForEach) {
            report(
                Finding(Entity.from(callee), "Java Map.forEach method call should be replaced with Kotlin's forEach"),
            )
        }
    }
}
