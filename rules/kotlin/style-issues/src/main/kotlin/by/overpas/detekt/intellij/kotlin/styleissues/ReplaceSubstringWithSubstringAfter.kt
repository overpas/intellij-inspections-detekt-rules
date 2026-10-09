package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

@IntellijInspection("ReplaceSubstringWithSubstringAfter")
class ReplaceSubstringWithSubstringAfter(config: Config) :
    Rule(
        config,
        "A `substring(s.indexOf(x))` call gets the part of the string from the delimiter. " +
            "Replace it with `substringAfter(x)`.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val arguments = expression.replaceSubstringArguments()
        if (arguments.size != 1 || !arguments.first().isReplaceSubstringCallTo("indexOf")) return
        val isReplaceable = analyze(expression) {
            expression.isReplaceSubstringStdlibCall() &&
                arguments.first().isReplaceSubstringIndexOfOn(expression.receiverExpression) &&
                expression.receiverExpression.isReplaceSubstringPure()
        }
        if (isReplaceable) {
            report(Finding(Entity.from(expression), "'substring' call should be replaced with 'substringAfter'"))
        }
    }
}
