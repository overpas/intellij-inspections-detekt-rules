package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.psiUtil.isNull

internal class LiftReturnOrAssignmentMatcher(
    private val session: KaSession,
    private val first: KtBinaryExpression,
) {

    private val leftType = with(session) { first.left?.expressionType }

    private val rightType = with(session) { first.right?.expressionType }

    private val leftSymbol = context(session) { first.left?.liftReferencedSymbol() }

    private val operationSymbol = context(session) { first.operationReference.liftReferencedSymbol() }

    fun matches(second: KtBinaryExpression): Boolean {
        val secondLeftSymbol = context(session) { second.left?.liftReferencedSymbol() }
        val secondOperationSymbol = context(session) { second.operationReference.liftReferencedSymbol() }
        val hasSameTarget = leftSymbol != null &&
            leftSymbol == secondLeftSymbol &&
            first.operationToken == second.operationToken
        val isNullAssignment = second.right?.isNull() == true && with(session) { leftType?.isNullable == true }
        val isSameOperation = operationSymbol != null && operationSymbol == secondOperationSymbol
        val hasTypes = leftType != null && rightType != null
        return hasSameTarget && hasTypes && (isNullAssignment || isSameOperation || second.hasCompatibleRightType())
    }

    private fun KtBinaryExpression.hasCompatibleRightType(): Boolean =
        with(session) {
            val type = right?.expressionType
                ?.takeIf { leftType?.isNullable == true || !it.isNullable }
                ?.withNullability(false)
            val isSameType = type != null && rightType?.semanticallyEquals(type) == true
            val isSubtype = type != null &&
                operationToken == KtTokens.EQ &&
                leftType?.let { type.isSubtypeOf(it) } == true
            isSameType || isSubtype
        }
}
