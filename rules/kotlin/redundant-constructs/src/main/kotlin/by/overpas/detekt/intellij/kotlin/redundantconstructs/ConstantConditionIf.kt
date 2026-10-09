package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

class ConstantConditionIf(config: Config) :
    Rule(
        config,
        "The condition of the if is a `true` or `false` literal, so one branch is dead. Simplify the if.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val condition = KtPsiUtil.safeDeparenthesize(expression.condition ?: return)
        if (condition.node.elementType != KtNodeTypes.BOOLEAN_CONSTANT) return
        report(Finding(Entity.from(condition), "Condition is always '${condition.text}'"))
    }
}
