package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBreakExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtContinueExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

internal class LiftReturnOrAssignmentFolding<T : KtExpression>(
    private val session: KaSession,
    private val isReturnExit: Boolean,
    private val foldable: (KtExpression?) -> T?,
) {

    fun collect(expression: KtExpression?): List<T>? {
        val isJump =
            expression is KtBreakExpression || expression is KtContinueExpression || expression is KtThrowExpression
        val isExitReturn = isReturnExit && expression is KtReturnExpression
        val isNothingCall = expression is KtCallExpression &&
            with(session) { expression.expressionType?.isNothingType == true }
        val hasMissingCases = expression is KtWhenExpression && context(session) { expression.hasLiftMissingCases() }
        val branchResults = expression
            ?.takeUnless { hasMissingCases }
            ?.liftReturnOrAssignmentBranches()
            ?.map { branch -> foldable(branch)?.let { listOf(it) } ?: collect(branch?.lastBlockStatementOrThis()) }
        val isExit = isJump || isExitReturn || isNothingCall
        return if (isExit) emptyList() else branchResults?.takeUnless { null in it }?.flatMap { it.orEmpty() }
    }
}
