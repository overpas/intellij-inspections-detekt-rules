package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

class ReplaceToStringWithStringTemplate(config: Config) :
    Rule(
        config,
        "A `toString()` call on a reference can be written as a string template. Use a string template instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val call = expression.selectorExpression as? KtCallExpression ?: return
        val callee = call.calleeExpression as? KtNameReferenceExpression ?: return
        val isCandidate = expression.receiverExpression is KtReferenceExpression &&
            expression.parent !is KtBlockStringTemplateEntry &&
            call.valueArguments.isEmpty() &&
            callee.getReferencedName() == "toString"
        if (isCandidate && call.isStringConversion()) {
            report(Finding(Entity.from(expression), "Call of 'toString' could be replaced with string template"))
        }
    }

    private fun KtCallExpression.isStringConversion(): Boolean =
        analyze(this) {
            val function = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            function != null && function.valueParameters.isEmpty() && function.returnType.isStringType
        }
}
