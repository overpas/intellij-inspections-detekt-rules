package by.overpas.detekt.intellij.kotlin.codemigration.canconverttomultidollarstring

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.OppositeRule
import by.overpas.detekt.intellij.kotlin.codemigration.convertfrommultidollartoregularstring.ConvertFromMultiDollarToRegularString
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@OppositeRule(ConvertFromMultiDollarToRegularString::class)
@IntellijInspection("CanConvertToMultiDollarString")
class CanConvertToMultiDollarString(config: Config) :
    Rule(
        config,
        $$"A string that escapes dollar characters, or writes one as `${'$'}`, can use an " +
            "interpolation prefix instead. Add a prefix such as `$$` and write the dollar characters as they are.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "The number of dollar characters that a string can escape with a backslash without a report. " +
            "With 0, every escaped dollar is reported, as in IntelliJ.",
    )
    private val allowedEscapedDollarCount: Int by config(1)

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
        val isAllowed = dollars.size <= allowedEscapedDollarCount &&
            dollars.all { it is KtEscapeStringTemplateEntry }
        if (scan.isConvertible() && !isAllowed) {
            dollars.forEach { report(Finding(Entity.from(it), "An interpolation prefix can simplify the string")) }
        }
    }
}
