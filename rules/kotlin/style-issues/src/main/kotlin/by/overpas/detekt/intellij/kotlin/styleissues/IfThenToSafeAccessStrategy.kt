package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal class IfThenToSafeAccessStrategy(
    private val session: KaSession,
    private val data: IfThenToSafeAccessData,
) {

    fun isSuggested(): Boolean? =
        context(session) {
            val checked = data.checkedExpression
            val base = data.baseClause
            val isSafeCast = data.condition is KtIsExpression
            if (checked is KtThisExpression && data.checkedUsages().isEmpty()) {
                val leftMost = base.ifThenLeftMostReceiver()
                val owners = leftMost.ifThenCallee()?.ifThenImplicitReceiverOwners().orEmpty()
                val isMatching = checked.ifThenTargetSymbol()?.let { it in owners } == true
                (!base.hasIfThenVariableCallsFrom(leftMost) && !isSafeCast).takeIf { isMatching }
            } else {
                val receiver = matchingReceiver()
                receiver != null &&
                    !base.hasIfThenVariableCallsFrom(receiver) &&
                    (!isSafeCast || base.isIfThenSimplifiableTo(checked))
            }
        }

    private fun matchingReceiver(): KtExpression? =
        context(session) {
            val target = data.checkedExpression.ifThenTargetSymbol()
            val leftMost = data.baseClause.ifThenLeftMostReceiver()
            val calleeTarget = (leftMost as? KtCallExpression)?.calleeExpression?.ifThenTargetSymbol()
            when {
                target == null -> null
                calleeTarget == target -> leftMost
                else -> leftMost.ifThenParentsUpTo(data.baseClause).firstOrNull { it.ifThenTargetSymbol() == target }
            }
        }
}
