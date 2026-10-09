package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class ReplaceAssociateFunction(config: Config) :
    Rule(
        config,
        "An `associate` call whose lambda returns a pair is harder to read than the dedicated function. " +
            "Use `associateBy` or `associateWith` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val callee = (expression.selectorExpression as? KtCallExpression)?.calleeExpression ?: return
        val replacement = expression.associateReplacement() ?: return
        report(Finding(Entity.from(callee), "Replace '${callee.text}' with '$replacement'"))
    }
}
