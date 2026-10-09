package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtIfExpression

class IfExpressionWithIdenticalBranches(config: Config) :
    Rule(
        config,
        "Both branches of the if do the same thing, so the condition has no effect. " +
            "Replace the if with one of its branches.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val thenBranch = expression.then?.let { IfExpressionWithIdenticalBranchesBranch(it) }
        val elseBranch = expression.`else`?.let { IfExpressionWithIdenticalBranchesBranch(it) }
        if (thenBranch == null || elseBranch == null || thenBranch.tokens() != elseBranch.tokens()) return
        if (thenBranch.resolvesLike(elseBranch)) {
            report(Finding(Entity.from(expression), "'if' expression has identical branches"))
        }
    }
}
