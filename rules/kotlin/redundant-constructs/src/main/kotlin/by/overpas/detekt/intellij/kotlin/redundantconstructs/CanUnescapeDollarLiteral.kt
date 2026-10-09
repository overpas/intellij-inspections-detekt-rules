package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class CanUnescapeDollarLiteral(config: Config) :
    Rule(
        config,
        "An escaped dollar in a string can be a plain `$` when it cannot start a template. " +
            "Replace the escaped dollar with a dollar literal.",
    ),
    RequiresAnalysisApi {

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        val initial = CanUnescapeDollarLiteralScan(prefixLength = expression.interpolationPrefix?.textLength ?: 0)
        val scan = expression.entries.fold(initial) { current, entry -> current.accept(entry) }
        if (scan.hasReplaceableDollars()) {
            report(Finding(Entity.from(expression), "Escaped dollar characters in the string can be simplified"))
        }
    }
}
