package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val REPLACE_WITH_IGNORE_CASE_EQUALS_CONVERSIONS =
    listOf("toUpperCase", "toLowerCase", "lowercase", "uppercase").map { FqName("kotlin.text.$it") }

internal class ReplaceWithIgnoreCaseEqualsOperands(private val expression: KtBinaryExpression) {

    private val calls = listOf(expression.left, expression.right)
        .map { (it as? KtQualifiedExpression)?.selectorExpression as? KtCallExpression ?: it as? KtCallExpression }

    fun isCandidate(): Boolean =
        expression.operationToken == KtTokens.EQEQ &&
            calls.map { it?.calleeExpression?.text }.distinct().singleOrNull() in
            REPLACE_WITH_IGNORE_CASE_EQUALS_CONVERSIONS.map { it.shortName().asString() }

    fun haveSameConversion(): Boolean =
        analyze(expression) {
            calls
                .map { call ->
                    call?.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId?.asSingleFqName() }
                }
                .distinct()
                .singleOrNull() in REPLACE_WITH_IGNORE_CASE_EQUALS_CONVERSIONS
        }
}
