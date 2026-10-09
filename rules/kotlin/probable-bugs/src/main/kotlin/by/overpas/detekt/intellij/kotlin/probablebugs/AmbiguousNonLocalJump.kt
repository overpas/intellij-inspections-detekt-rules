package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBreakExpression
import org.jetbrains.kotlin.psi.KtContinueExpression
import org.jetbrains.kotlin.psi.KtExpressionWithLabel

class AmbiguousNonLocalJump(config: Config) :
    Rule(
        config,
        "An unlabeled `break` or `continue` inside an inline lambda in a loop can be read as a jump out of the " +
            "lambda's call. Add a label to the jump.",
    ),
    RequiresAnalysisApi {

    override fun visitBreakExpression(expression: KtBreakExpression) {
        super.visitBreakExpression(expression)
        expression.reportIfAmbiguous()
    }

    override fun visitContinueExpression(expression: KtContinueExpression) {
        super.visitContinueExpression(expression)
        expression.reportIfAmbiguous()
    }

    private fun KtExpressionWithLabel.reportIfAmbiguous() {
        AmbiguousNonLocalJumpCandidate(this).message()?.let { report(Finding(Entity.from(this), it)) }
    }
}
