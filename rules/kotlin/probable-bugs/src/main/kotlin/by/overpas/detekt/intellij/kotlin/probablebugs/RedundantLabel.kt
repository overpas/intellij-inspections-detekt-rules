package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiUtil

@IntellijInspection("RedundantLabel")
class RedundantLabel(config: Config) :
    Rule(
        config,
        "A label on an expression that is not a lambda, a loop or a function cannot be referenced. Remove the label.",
    ),
    RequiresAnalysisApi {

    override fun visitLabeledExpression(expression: KtLabeledExpression) {
        super.visitLabeledExpression(expression)
        val label = expression.getTargetLabel() ?: return
        val target = KtPsiUtil.deparenthesize(expression)
        if (target !is KtLambdaExpression && target !is KtLoopExpression && target !is KtNamedFunction) {
            report(Finding(Entity.from(label), "Redundant label"))
        }
    }
}
