package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

internal const val WITH_NAME = "with"

internal fun KtCallExpression.withLambda(): KtLambdaExpression? =
    valueArguments
        .takeIf { it.size == 2 && it.first().getArgumentExpression() != null }
        ?.run { last().withLambdaExpression() }
        ?.takeIf { it.bodyExpression != null }

context(session: KaSession)
internal fun KtCallExpression.isRedundantWithCall(lambda: KtLambdaExpression): Boolean {
    val statementCount = lambda.bodyExpression?.run { statements.size } ?: 0
    val isUsedAsExpression = with(session) { isUsedAsExpression }
    return isKotlinWithCall() &&
        !lambda.functionLiteral.usesWithReceiver() &&
        (statementCount <= 1 || !isUsedAsExpression || isWithFunctionBody())
}
