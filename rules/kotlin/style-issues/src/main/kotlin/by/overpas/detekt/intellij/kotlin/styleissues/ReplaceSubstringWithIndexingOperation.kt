package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

@IntellijInspection("ReplaceSubstringWithIndexingOperation")
class ReplaceSubstringWithIndexingOperation(config: Config) :
    Rule(
        config,
        "A `substring(i, i + 1)` call with constant indices gets one character as a string. " +
            "Replace it with the indexing operator `s[i]`.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val arguments = expression.replaceSubstringArguments()
        if (arguments.size != 2 || arguments.any { it !is KtConstantExpression }) return
        val isReplaceable = analyze(expression) {
            expression.isReplaceSubstringStdlibCall() &&
                arguments.first().replaceSubstringIntValue()?.plus(1) == arguments.last().replaceSubstringIntValue()
        }
        if (isReplaceable) {
            report(Finding(Entity.from(expression), "'substring' call should be replaced with indexing operator"))
        }
    }
}
