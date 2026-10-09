package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFinallySection
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isNull

context(session: KaSession)
internal fun KtExpression.liftReturnOrAssignmentType(): String? {
    val returns = LiftReturnOrAssignmentFolding(session, isReturnExit = false) { it.liftableReturn() }
        .collect(this)
        .orEmpty()
        .toSet()
    val assignments = LiftReturnOrAssignmentFolding(session, isReturnExit = true) { it.liftableAssignment() }
        .collect(this)
        .orEmpty()
        .toSet()
    val isUsed = parent !is KtBlockExpression && with(session) { isUsedAsExpression }
    val isLiftableReturn = returns.size > 1 &&
        returns.all(KtExpression::isLiftSingleStatement) &&
        !anyDescendantOfType<KtReturnExpression> { it !in returns }
    val isLiftableAssignment = assignments.size > 1 &&
        assignments.all(KtExpression::isLiftSingleStatement) &&
        hasLiftableAssignments(assignments)
    return when {
        isUsed -> null
        returns.isNotEmpty() -> "Return".takeIf { isLiftableReturn }
        isLiftableAssignment -> "Assignment"
        else -> null
    }
}

context(session: KaSession)
private fun KtExpression.hasLiftableAssignments(assignments: Set<KtBinaryExpression>): Boolean {
    val first = assignments.firstOrNull { it.right?.isNull() != true } ?: assignments.first()
    val matcher = LiftReturnOrAssignmentMatcher(session, first)
    return assignments.all(matcher::matches) &&
        !anyDescendantOfType<KtBinaryExpression> { assignment ->
            val isInFinally = assignment.getNonStrictParentOfType<KtFinallySection>() != null
            assignment.operationToken in KtTokens.ALL_ASSIGNMENTS &&
                if (isInFinally) matcher.matches(assignment) else assignment !in assignments
        }
}
