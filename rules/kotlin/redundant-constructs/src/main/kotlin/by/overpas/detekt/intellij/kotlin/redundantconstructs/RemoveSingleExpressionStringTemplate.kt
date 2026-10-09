package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class RemoveSingleExpressionStringTemplate(config: Config) :
    Rule(
        config,
        "A string template that holds only one `String` expression is redundant. Use the expression directly.",
    ),
    RequiresAnalysisApi {

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        val single = expression.children.singleOrNull()?.run { children.firstOrNull() } as? KtExpression ?: return
        val isString = analyze(single) {
            val type = single.expressionType
            type != null && type.isClassType(StandardClassIds.String) && !type.isMarkedNullable
        }
        if (isString) report(Finding(Entity.from(expression), "Redundant single-expression string template"))
    }
}
