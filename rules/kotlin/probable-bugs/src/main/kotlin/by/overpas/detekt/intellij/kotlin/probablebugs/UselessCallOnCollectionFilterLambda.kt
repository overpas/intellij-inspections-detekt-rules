package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression

@OptIn(KaExperimentalApi::class)
internal class UselessCallOnCollectionFilterLambda(
    session: KaSession,
    lambda: KtLambdaExpression,
) {

    private val statement = lambda.bodyExpression?.statements.orEmpty().singleOrNull()

    private val isReturnFromLambda = (statement as? KtReturnExpression)
        ?.let { with(session) { it.resolveSymbol()?.psi } } == lambda.functionLiteral

    private val value = if (statement is KtReturnExpression) {
        statement.returnedExpression?.takeIf { isReturnFromLambda }
    } else {
        statement
    }?.let { KtPsiUtil.safeDeparenthesize(it) }

    val isTrueConstant: Boolean = KtPsiUtil.isTrueConstant(value)

    val isFalseConstant: Boolean = KtPsiUtil.isFalseConstant(value)
}
