package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.util.OperatorNameConventions

@IntellijInspection("KotlinEqualsBetweenInconvertibleTypes")
class KotlinEqualsBetweenInconvertibleTypes(config: Config) :
    Rule(
        config,
        "An `equals()` call between primitives, strings or enums of different types always returns false. " +
            "Compare values of the same type.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression
        val isEqualsCall =
            (callee as? KtSimpleNameExpression)?.getReferencedNameAsName() == OperatorNameConventions.EQUALS
        if (callee != null && isEqualsCall && analyze(expression) { expression.isInconvertibleEqualsCall() }) {
            report(Finding(Entity.from(callee), "'equals()' between objects of inconvertible types"))
        }
    }
}
