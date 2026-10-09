package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class ReplaceSubstringWithSubstringBefore(config: Config) :
    Rule(
        config,
        "A `substring(0, s.indexOf(x))` call gets the part of the string before the delimiter. " +
            "Replace it with `substringBefore(x)`.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val arguments = expression.replaceSubstringArguments()
        if (!arguments.isReplaceSubstringFromZero() || !arguments.last().isReplaceSubstringCallTo("indexOf")) return
        val isReplaceable = analyze(expression) {
            expression.isReplaceSubstringStdlibCall() &&
                arguments.first().replaceSubstringIntValue() == 0 &&
                arguments.last().isReplaceSubstringIndexOfOn(expression.receiverExpression) &&
                expression.receiverExpression.isReplaceSubstringPure()
        }
        if (isReplaceable) {
            report(Finding(Entity.from(expression), "'substring' call should be replaced with 'substringBefore'"))
        }
    }
}
