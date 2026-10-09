package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

private val replaceCallWithBinaryOperatorAnyEquals = CallableId(StandardClassIds.Any, Name.identifier("equals"))

private val replaceCallWithBinaryOperatorComparisons = setOf(KtTokens.LT, KtTokens.GT, KtTokens.LTEQ, KtTokens.GTEQ)

internal class ReplaceCallWithBinaryOperatorCandidate(
    session: KaSession,
    expression: KtDotQualifiedExpression,
    symbol: KaFunctionSymbol,
    argument: KtExpression,
) {

    private val receiverType = with(session) { expression.receiverExpression.expressionType }

    private val argumentType = with(session) { argument.expressionType }

    private val floatingPointOperands = with(session) {
        listOfNotNull(receiverType, argumentType)
            .count { it.isSubtypeOf(builtinTypes.double) || it.isSubtypeOf(builtinTypes.float) }
    }

    private val isOperator = (symbol as? KaNamedFunctionSymbol)?.isOperator == true

    private val isReplaceableEquals = with(session) {
        val isRelated = receiverType != null &&
            argumentType != null &&
            (receiverType.isSubtypeOf(argumentType) || argumentType.isSubtypeOf(receiverType))
        val wrapper = generateSequence(expression.parent) { it.parent }
            .dropWhile { it is KtParenthesizedExpression }
            .firstOrNull() as? KtPrefixExpression
        val isNegated = wrapper?.operationToken == KtTokens.EXCL
        val overridden = sequenceOf(symbol) + symbol.allOverriddenSymbols
        val isAnyEquals = overridden.any { it.callableId == replaceCallWithBinaryOperatorAnyEquals }
        isRelated && isAnyEquals && (isNegated || floatingPointOperands != 1)
    }

    private val isReplaceableCompareTo = (expression.parent as? KtBinaryExpression)?.let { comparison ->
        val zero = if (comparison.left == expression) comparison.right else comparison.left
        val isZero = zero is KtConstantExpression &&
            zero.node.elementType == KtNodeTypes.INTEGER_CONSTANT &&
            zero.text == "0"
        isZero && comparison.operationToken in replaceCallWithBinaryOperatorComparisons
    } == true && isOperator && floatingPointOperands == 0

    val isReplaceable = when ((expression.selectorExpression as? KtCallExpression)?.calleeExpression?.text) {
        "equals" -> isReplaceableEquals
        "compareTo" -> isReplaceableCompareTo
        else -> isOperator
    }
}
