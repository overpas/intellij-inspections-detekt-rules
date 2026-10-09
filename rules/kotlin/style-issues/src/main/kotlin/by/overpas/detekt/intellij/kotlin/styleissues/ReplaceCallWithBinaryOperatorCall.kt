package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

internal fun KtDotQualifiedExpression.isBinaryOperatorReplaceable(): Boolean {
    val expression = this
    val call = selectorExpression as? KtCallExpression
    val argument = call?.valueArguments?.singleOrNull()?.getArgumentExpression()
    return call != null &&
        argument != null &&
        analyze(call) {
            val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
            val isResolved = resolved != null &&
                resolved.symbol.valueParameters.size == 1 &&
                resolved.typeArgumentsMapping.isEmpty() &&
                expression.receiverExpression.expressionType != null
            isResolved &&
                ReplaceCallWithBinaryOperatorCandidate(
                    session = this,
                    expression = expression,
                    symbol = resolved.symbol,
                    argument = argument,
                ).isReplaceable
        }
}
