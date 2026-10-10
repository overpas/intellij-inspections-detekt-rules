package by.overpas.detekt.intellij.kotlin.codemigration

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.OppositeRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@OppositeRule(CanConvertToMultiDollarString::class)
@IntellijInspection("ConvertFromMultiDollarToRegularString")
class ConvertFromMultiDollarToRegularString(config: Config) :
    Rule(
        config,
        "A string with an interpolation prefix can be written as a regular string. " +
            "Remove the prefix and escape the dollar characters where it reads better.",
    ),
    RequiresAnalysisApi {

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        if (expression.interpolationPrefix != null) {
            report(Finding(Entity.from(expression), "String prefix can be removed"))
        }
    }
}
