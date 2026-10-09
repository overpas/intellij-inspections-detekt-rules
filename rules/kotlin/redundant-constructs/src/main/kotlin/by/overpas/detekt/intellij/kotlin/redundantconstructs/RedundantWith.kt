package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("RedundantWith")
class RedundantWith(config: Config) :
    Rule(
        config,
        "A `with` call whose lambda does not use the receiver is redundant. Replace it with the lambda body.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.takeIf { it.text == WITH_NAME } ?: return
        val lambda = expression.withLambda() ?: return
        if (analyze(expression) { expression.isRedundantWithCall(lambda) }) {
            report(Finding(Entity.from(callee), "Redundant 'with' call"))
        }
    }
}
