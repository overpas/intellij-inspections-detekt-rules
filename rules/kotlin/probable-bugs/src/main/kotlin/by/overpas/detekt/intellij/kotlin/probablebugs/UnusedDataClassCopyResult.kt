package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

class UnusedDataClassCopyResult(config: Config) :
    Rule(
        config,
        "The `copy` call of a data class returns a new object and does not change the receiver. " +
            "Use the result or remove the call.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.takeIf { it.text == COPY } ?: return
        val isUnusedCopy = analyze(expression) {
            val symbol = expression.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val owner = symbol?.containingDeclaration as? KaNamedClassSymbol
            val qualified = expression.getQualifiedExpressionForSelectorOrThis()
            owner?.isData == true && !qualified.isUsedAsExpression
        }
        if (isUnusedCopy) report(Finding(Entity.from(callee), "Unused result of data class copy"))
    }

    private companion object {
        const val COPY = "copy"
    }
}
