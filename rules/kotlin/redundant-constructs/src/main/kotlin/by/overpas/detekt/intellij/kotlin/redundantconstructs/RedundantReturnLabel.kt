package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

@IntellijInspection("RedundantReturnLabel")
class RedundantReturnLabel(config: Config) :
    Rule(
        config,
        "A label on a `return` from a function that is not a lambda is redundant. Remove the label.",
    ),
    RequiresAnalysisApi {

    override fun visitReturnExpression(expression: KtReturnExpression) {
        super.visitReturnExpression(expression)
        val label = expression.getTargetLabel() ?: return
        val function = expression.getParentOfType<KtNamedFunction>(true, KtLambdaExpression::class.java)
        if (function != null && (function.name != null || expression.targetsFunction(function))) {
            report(Finding(Entity.from(label), "Redundant '${label.getReferencedName()}'"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtReturnExpression.targetsFunction(function: KtNamedFunction): Boolean =
        analyze(this) { resolveSymbol() == function.symbol }
}
