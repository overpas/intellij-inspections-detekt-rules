package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("ConvertToStringTemplate")
class ConvertToStringTemplate(config: Config) :
    Rule(
        config,
        "A `String` concatenation of literals and simple values is harder to read than a string template. " +
            "Convert the concatenation to a string template.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val parent = expression.parent as? KtBinaryExpression
        val isConvertible = ConvertToStringTemplateLines.isSingleLine(expression) &&
            !expression.textContains('\n') &&
            analyze(expression) {
                val plus = ConvertToStringTemplatePlus(this)
                val isNested = parent != null &&
                    ConvertToStringTemplateLines.isSingleLine(parent) &&
                    plus.isStringPlus(parent)
                !isNested &&
                    plus.isStringPlus(expression) &&
                    ConvertToStringTemplateBuilder(this, plus).hasOnlySimpleEntries(expression)
            }
        if (isConvertible) {
            report(Finding(Entity.from(expression), "'String' concatenation can be converted to a template"))
        }
    }
}
