package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtReturnExpression

internal fun KtDeclarationWithBody.replacementExpression(isUnitReturned: Boolean): KtExpression? {
    val body = bodyExpression
    val statement = (body as? KtBlockExpression)?.run { statements.singleOrNull() }
    return when {
        !hasBlockBody() -> body
        statement is KtReturnExpression -> statement.returnedExpression
        isUnitReturned -> statement
        else -> null
    }
}
