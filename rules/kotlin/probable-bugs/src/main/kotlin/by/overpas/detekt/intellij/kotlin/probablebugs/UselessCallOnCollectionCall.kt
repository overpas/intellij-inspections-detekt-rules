package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeProjection
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal const val USELESS_CALL_ON_COLLECTION_REDUNDANT = "Redundant call on collection type"

internal data class UselessCallOnCollectionCall(
    val session: KaSession,
    val expression: KtQualifiedExpression,
    val call: KaFunctionCall<*>,
) {

    val receiverTypeArgument: KaTypeProjection? =
        with(session) { expression.receiverExpression.expressionType as? KaClassType }
            ?.let { it.typeArguments.singleOrNull() }

    val elementType: KaType? = receiverTypeArgument?.type

    val isElementTypeNotNull: Boolean = elementType?.let { with(session) { !it.isNullable } } == true

    val lambda: KtLambdaExpression? = (expression.selectorExpression as? KtCallExpression)
        ?.valueArguments
        ?.lastOrNull()
        ?.getArgumentExpression() as? KtLambdaExpression
}
