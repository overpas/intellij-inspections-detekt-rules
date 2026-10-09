package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

context(session: KaSession)
internal fun KtBinaryExpression.isSimplifiableBooleanRoot(): Boolean {
    val enclosing = parents.firstOrNull { it !is KtParenthesizedExpression } as? KtBinaryExpression
    val enclosingRoot = enclosing?.run { parents.takeWhile { it is KtBinaryExpression }.lastOrNull() ?: this }
    return hasBooleanConstantsToSimplify() &&
        (enclosingRoot as? KtBinaryExpression)?.hasBooleanConstantsToSimplify() != true
}
