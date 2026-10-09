package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class ReplaceSubstringWithTake(config: Config) :
    Rule(
        config,
        "A `substring(0, n)` call takes the first characters of the string. Replace it with `take(n)`.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val arguments = expression.replaceSubstringArguments()
        if (!arguments.isReplaceSubstringFromZero() || expression.isReplaceSubstringDropLast(arguments)) return
        val isReplaceable = analyze(expression) {
            expression.isReplaceSubstringStdlibCall() &&
                expression.receiverExpression.isReplaceSubstringPure() &&
                arguments.first().replaceSubstringIntValue() == 0 &&
                expression.isReplaceSubstringChainSafe()
        }
        if (isReplaceable) {
            report(Finding(Entity.from(expression), "'substring' call should be replaced with 'take' call"))
        }
    }
}
