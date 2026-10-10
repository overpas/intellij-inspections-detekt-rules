package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("SimplifyWhenWithBooleanConstantCondition")
class SimplifyWhenWithBooleanConstantCondition(config: Config) :
    Rule(
        config,
        "A subjectless when with a `true` or `false` branch condition has dead or unconditional branches. " +
            "Simplify the when.",
    ) {

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        if (expression.closeBrace == null || expression.subjectExpression != null) return
        if (expression.entries.none { it.hasBooleanConstantCondition() }) return
        report(Finding(Entity.from(expression), "This 'when' is simplifiable"))
    }

    private fun KtWhenEntry.hasBooleanConstantCondition(): Boolean {
        val condition = conditions.singleOrNull() as? KtWhenConditionWithExpression
        return condition?.expression?.node?.elementType == KtNodeTypes.BOOLEAN_CONSTANT
    }
}
