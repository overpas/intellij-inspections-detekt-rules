package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression

private const val LIFT_RETURN_OR_ASSIGNMENT_MAX_LINES = 15

internal fun KtExpression.liftReturnOrAssignmentKeyword(): PsiElement? {
    val keyword = when (this) {
        is KtWhenExpression -> whenKeyword
        is KtIfExpression -> ifKeyword
        is KtTryExpression -> tryKeyword
        else -> null
    }
    val isShort = text.count { it == '\n' } < LIFT_RETURN_OR_ASSIGNMENT_MAX_LINES
    return keyword?.takeIf { isShort && parent.node.elementType != KtNodeTypes.ELSE }
}

internal fun KtExpression.liftReturnOrAssignmentBranches(): List<KtExpression?>? =
    when (this) {
        is KtWhenExpression -> entries.takeIf { it.isNotEmpty() }?.map { it.expression }
        is KtIfExpression -> liftIfBranches()
        is KtTryExpression -> listOf(tryBlock) + catchClauses.map { it.catchBody }
        else -> null
    }

private fun KtIfExpression.liftIfBranches(): List<KtExpression?> {
    val elseBranch = `else`
    return listOf(then) + if (elseBranch is KtIfExpression) elseBranch.liftIfBranches() else listOf(elseBranch)
}
