package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

internal class ForEachJoinOnCollectionOfJobCall(
    private val session: KaSession,
    private val expression: KtCallExpression,
    private val functionId: CallableId,
) {

    fun collectionLambda(): ForEachJoinOnCollectionOfJobLambda? =
        with(session) {
            val lambda = expression.valueArguments.singleOrNull()?.getArgumentExpression() as? KtLambdaExpression
            val call = lambda?.let { expression.resolveToCall()?.successfulFunctionCallOrNull() } ?: return null
            val isCollectionCall = call.symbol.callableId == functionId &&
                call.symbol.receiverParameter?.run { returnType.isSubtypeOf(StandardClassIds.Iterable) } == true &&
                call.extensionReceiver?.run { type.isSubtypeOf(StandardClassIds.Collection) } == true
            lambda.takeIf { isCollectionCall }?.let { ForEachJoinOnCollectionOfJobLambda(session, it) }
        }
}
