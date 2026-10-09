package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceStringFormatWithLiteral(config: Config) :
    Rule(
        config,
        "A `String.format` call that uses only `%s` placeholders is harder to read than a string template. " +
            "Replace it with a string template.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val target = expression.replaceStringFormatTarget() ?: return
        if (!expression.hasReplaceStringFormatArgs()) return
        if (analyze(expression) { expression.isReplaceStringFormatCall() }) {
            report(Finding(Entity.from(target), "'String.format' call can be replaced with string templates"))
        }
    }
}
