package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class ForEachJoinOnCollectionOfJobLambda(
    private val session: KaSession,
    private val lambda: KtLambdaExpression,
) {

    fun returnsCallOnParameter(callableId: CallableId): Boolean =
        with(session) {
            val parameter = lambda.functionLiteral.symbol.valueParameters.singleOrNull()
            val returned = singleReturnedExpression() as? KtDotQualifiedExpression
            val call = returned?.resolveToCall()?.successfulFunctionCallOrNull()
            val receiver = (call?.dispatchReceiver as? KaExplicitReceiverValue)?.expression
            val receiverSymbol = receiver?.run {
                references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
            }
            parameter != null && call?.run { symbol.callableId } == callableId && receiverSymbol == parameter
        }

    private fun singleReturnedExpression(): KtExpression? {
        val statement = lambda.bodyExpression?.run { statements.singleOrNull() }
        val labels = setOfNotNull(
            (lambda.parent as? KtLabeledExpression)?.getLabelName(),
            lambda.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text,
        )
        return if (statement is KtReturnExpression) {
            statement.returnedExpression.takeIf { statement.getLabelName() in labels }
        } else {
            statement
        }
    }
}
