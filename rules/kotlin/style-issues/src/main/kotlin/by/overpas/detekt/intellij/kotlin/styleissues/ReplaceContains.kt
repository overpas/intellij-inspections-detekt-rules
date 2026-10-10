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
import org.jetbrains.kotlin.psi.KtSuperExpression

@IntellijInspection("ReplaceContains")
class ReplaceContains(config: Config) :
    Rule(
        config,
        "A call of the `contains` operator function is less idiomatic than the `in` operator. " +
            "Use `in` or `!in` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val call = expression.selectorExpression as? KtCallExpression ?: return
        val callee = call.calleeExpression ?: return
        val isCandidate = expression.receiverExpression !is KtSuperExpression &&
            callee.text == "contains" &&
            call.valueArguments.size == 1
        if (isCandidate && expression.isOperatorContainsCall()) {
            report(
                Finding(
                    Entity.from(callee),
                    "Usage of Java 'contains' call instead of Kotlin idiomatic 'in' operator",
                ),
            )
        }
    }

    private fun KtDotQualifiedExpression.isOperatorContainsCall(): Boolean =
        analyze(this) {
            val call = (selectorExpression as? KtCallExpression)?.resolveToCall()?.successfulFunctionCallOrNull()
            val symbol = call?.symbol as? KaNamedFunctionSymbol
            val parameter = call?.valueArgumentMapping?.values?.singleOrNull()?.symbol
            symbol != null &&
                symbol.isOperator &&
                symbol.returnType.isBooleanType &&
                symbol.valueParameters.indexOf(parameter) == 0 &&
                receiverExpression.expressionType != null
        }
}
