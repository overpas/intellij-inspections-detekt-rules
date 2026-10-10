package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

@IntellijInspection("CopyWithoutNamedArguments")
class CopyWithoutNamedArguments(config: Config) :
    Rule(
        config,
        "Positional arguments of a data class `copy` call are easy to mix up. Name every argument.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
        val isCandidate = callee.getReferencedName() == "copy" && expression.valueArguments.any { !it.isNamed() }
        if (isCandidate && analyze(expression) { expression.isDataClassCopy() }) {
            report(
                Finding(
                    Entity.from(callee),
                    "Parameter names should be specified explicitly for the 'copy()' method call",
                ),
            )
        }
    }

    context(session: KaSession)
    private fun KtCallExpression.isDataClassCopy(): Boolean =
        with(session) {
            val receiverClass = resolveToCall()?.successfulFunctionCallOrNull()?.dispatchReceiver?.run {
                type.expandedSymbol
            }
            (receiverClass as? KaNamedClassSymbol)?.isData == true
        }
}
