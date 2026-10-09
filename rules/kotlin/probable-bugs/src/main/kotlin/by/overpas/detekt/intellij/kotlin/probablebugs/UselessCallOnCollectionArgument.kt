package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

@OptIn(KaExperimentalApi::class)
internal class UselessCallOnCollectionArgument(
    session: KaSession,
    argument: KtExpression,
) {

    private val lambda = ((argument as? KtLabeledExpression)?.baseExpression ?: argument) as? KtLambdaExpression

    private val body = lambda?.bodyExpression

    private val hasNullableReturn = body?.collectDescendantsOfType<KtReturnExpression>().orEmpty().any { returned ->
        with(session) {
            returned.resolveSymbol()?.psi?.parent == lambda &&
                returned.returnedExpression?.expressionType?.isNullable == true
        }
    }

    private val isLambdaReturningNotNull = !hasNullableReturn &&
        with(session) { body?.expressionType?.isNullable == false }

    private val isReferenceReturningNotNull =
        with(session) { (argument.expressionType as? KaFunctionType)?.let { !it.returnType.isNullable } == true }

    val isReturningNotNull: Boolean = isLambdaReturningNotNull || isReferenceReturningNotNull
}
