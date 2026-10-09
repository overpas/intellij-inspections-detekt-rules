package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtOperationExpression

internal data class IfThenToSafeAccessData(
    val condition: KtOperationExpression,
    val checkedExpression: KtExpression,
    val clauses: IfThenToSafeAccessClauses,
) {

    val baseClause: KtExpression = clauses.base

    val negatedClause: KtExpression? = clauses.negated
}

internal fun KtIfExpression.ifThenToSafeAccessData(): IfThenToSafeAccessData? {
    val operation = condition?.ifThenSingleStatement() as? KtOperationExpression ?: return null
    val thenClause = then?.ifThenSingleStatement()
    val elseClause = `else`?.ifThenSingleStatement()
    val hasClauses = (then == null || thenClause != null) && (`else` == null || elseClause != null)
    val baseClause = when (operation.isPositiveIfThenCheck()) {
        true -> thenClause
        false -> elseClause
        null -> null
    }
    val clauses = baseClause?.takeIf { hasClauses }?.let { IfThenToSafeAccessClauses(it, this) }
    val checked = operation.ifThenCheckedExpression()?.ifThenSingleStatement()
    return checked?.let { checkedExpression ->
        clauses?.let { IfThenToSafeAccessData(operation, checkedExpression, it) }
    }
}
