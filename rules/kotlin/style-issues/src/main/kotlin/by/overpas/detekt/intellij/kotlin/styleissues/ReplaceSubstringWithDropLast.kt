package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class ReplaceSubstringWithDropLast(config: Config) :
    Rule(
        config,
        "A `substring(0, s.length - n)` call cuts the last characters of the string. Replace it with `dropLast(n)`.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val arguments = expression.replaceSubstringArguments()
        if (!arguments.isReplaceSubstringFromZero() || !expression.isReplaceSubstringDropLast(arguments)) return
        val isReplaceable = analyze(expression) {
            expression.isReplaceSubstringStdlibCall() && expression.receiverExpression.isReplaceSubstringPure()
        }
        if (isReplaceable) {
            report(Finding(Entity.from(expression), "'substring' call should be replaced with 'dropLast' call"))
        }
    }
}
