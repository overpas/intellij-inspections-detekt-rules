package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class CanConvertToMultiDollarString(config: Config) :
    Rule(
        config,
        "A string that escapes dollar characters can use an interpolation prefix instead. " +
            "Add a prefix such as `$$` and write the dollar characters as they are.",
    ),
    RequiresAnalysisApi {

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        super.visitStringTemplateExpression(expression)
        if (expression.interpolationPrefix != null) return
        val initial = CanConvertToMultiDollarStringScan(
            hasEscapedDollar = false,
            longestUnsafe = 0,
            sequentialDollars = 0,
        )
        val scan = expression.entries.fold(initial) { current, entry -> current.accept(entry) }
        if (scan.isConvertible()) {
            report(Finding(Entity.from(expression), "An interpolation prefix can simplify the string"))
        }
    }
}
