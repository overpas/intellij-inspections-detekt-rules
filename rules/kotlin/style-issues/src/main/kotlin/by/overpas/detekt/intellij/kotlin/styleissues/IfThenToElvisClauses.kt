package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal class IfThenToElvisClauses(
    private val session: KaSession,
    private val data: IfThenToSafeAccessData,
) {

    fun areReplaceable(): Boolean =
        context(session) {
            val negated = data.negatedClause
            val base = data.baseClause
            val checked = data.checkedExpression
            val hasImplicitReceiver = checked is KtThisExpression && base.ifThenImplicitReceiverOwners().isNotEmpty()
            when {
                negated == null || negated.isIfThenNullOrBlock() || negated.isIfThenNpeWithoutArguments() -> false
                hasIncompatibleTypes() -> false
                base.isIfThenSimplifiableTo(checked) || checked.isIfThenArgumentOf(base) -> true
                hasImplicitReceiver || checked.isIfThenFirstReceiverOf(base) -> base.hasNonNullIfThenReceivers()
                else -> false
            }
        }

    private fun hasIncompatibleTypes(): Boolean =
        with(session) {
            val condition = data.condition as? KtIsExpression
            val targetType = condition?.typeReference?.type
            val originalType = data.checkedExpression.expressionType
            val isUnrelated = targetType != null && originalType != null && !targetType.isSubtypeOf(originalType)
            condition != null && (targetType?.isNullable != false || originalType == null || isUnrelated)
        }
}
