package by.overpas.detekt.intellij.kotlin.codemigration.canconverttomultidollarstring

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.OppositeRule
import by.overpas.detekt.intellij.kotlin.codemigration.convertfrommultidollartoregularstring.ConvertFromMultiDollarToRegularString
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@OppositeRule(ConvertFromMultiDollarToRegularString::class)
@IntellijInspection("CanConvertToMultiDollarString")
class CanConvertToMultiDollarString(config: Config) :
    Rule(
        config,
        $$"A string that escapes two or more dollar characters, or writes one as `${'$'}`, can use an " +
            "interpolation prefix instead. Unlike IntelliJ, a single escaped dollar is not reported. " +
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
        val dollars = expression.entries.filter { it.isEscapedDollar }
        val isSingleBackslashEscape = dollars.singleOrNull() is KtEscapeStringTemplateEntry
        if (scan.isConvertible() && !isSingleBackslashEscape) {
            dollars.forEach { report(Finding(Entity.from(it), "An interpolation prefix can simplify the string")) }
        }
    }
}
