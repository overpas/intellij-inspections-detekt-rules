package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

@IntellijInspection("ReplaceToWithInfixForm")
class ReplaceToWithInfixForm(config: Config) :
    Rule(
        config,
        "A call of the infix function `to` in dot-call form is harder to read. Use the infix form `a to b` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val call = expression.selectorExpression as? KtCallExpression ?: return
        val callee = call.calleeExpression as? KtNameReferenceExpression ?: return
        val isCandidate = call.valueArguments.size == 1 &&
            call.typeArgumentList == null &&
            callee.getReferencedName() == "to"
        if (isCandidate && call.isInfixCall()) {
            report(Finding(Entity.from(expression), "'to' call should be replaced with infix form"))
        }
    }

    private fun KtCallExpression.isInfixCall(): Boolean =
        analyze(this) {
            (resolveToCall()?.successfulFunctionCallOrNull()?.symbol as? KaNamedFunctionSymbol)?.isInfix == true
        }
}
