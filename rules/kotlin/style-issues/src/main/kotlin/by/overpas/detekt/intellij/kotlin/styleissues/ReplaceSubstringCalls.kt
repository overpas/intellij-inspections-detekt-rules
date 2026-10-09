package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression

internal fun KtExpression?.isReplaceSubstringCallTo(name: String): Boolean =
    ((this as? KtDotQualifiedExpression)?.selectorExpression as? KtCallExpression)?.calleeExpression?.text == name

internal fun KtDotQualifiedExpression.replaceSubstringArguments(): List<KtExpression> =
    (selectorExpression as? KtCallExpression)
        ?.takeIf { isReplaceSubstringCallTo("substring") }
        ?.run { valueArguments.mapNotNull { it.getArgumentExpression() } }
        .orEmpty()

internal fun List<KtExpression>.isReplaceSubstringFromZero(): Boolean =
    size == 2 && (first() as? KtConstantExpression)?.text == "0"

internal fun KtDotQualifiedExpression.isReplaceSubstringDropLast(arguments: List<KtExpression>): Boolean {
    val length = (arguments.getOrNull(1) as? KtBinaryExpression)
        ?.takeIf { it.operationToken == KtTokens.MINUS && it.right != null }
        ?.left as? KtDotQualifiedExpression
    return length?.run {
        receiverExpression.text == this@isReplaceSubstringDropLast.receiverExpression.text &&
            selectorExpression?.text == "length"
    } == true
}
