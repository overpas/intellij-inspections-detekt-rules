package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal class VerboseNullabilityAndEmptinessNullCheck(
    private val check: KtExpression,
    val target: VerboseNullabilityAndEmptinessTarget,
    val isPositive: Boolean,
) {

    fun contentExpression(): KtExpression? {
        val wrapped = generateSequence(check) { child ->
            (child.parent as? KtExpression)?.takeIf { KtPsiUtil.deparenthesizeOnce(it) === child }
        }.last()
        val operation = if (isPositive) KtTokens.ANDAND else KtTokens.OROR
        val binary = (wrapped.parent as? KtBinaryExpression)?.takeIf { it.operationToken == operation }
        val content = when {
            binary == null -> null
            binary.left === wrapped -> binary.right
            else -> (binary.parent as? KtBinaryExpression)?.takeIf { it.operationToken == operation }?.right
        }
        return content?.let { KtPsiUtil.safeDeparenthesize(it) }
    }
}

internal fun KtBinaryExpression.verboseNullabilityNullCheck(): VerboseNullabilityAndEmptinessNullCheck? {
    val negations = generateSequence<KtExpression>(this) { child ->
        (child.parent as? KtExpression)?.takeIf { parent ->
            KtPsiUtil.deparenthesizeOnce(parent) === child ||
                (parent is KtPrefixExpression && parent.operationToken == KtTokens.EXCL)
        }
    }.filterIsInstance<KtPrefixExpression>().toList()
    val isNull = { side: KtExpression? -> side != null && KtPsiUtil.isNullConstant(side) }
    val target = (right.takeIf { isNull(left) } ?: left.takeIf { isNull(right) })?.verboseNullabilityTarget()
    val isNullityCheck = operationToken == KtTokens.EQEQ || operationToken == KtTokens.EXCLEQ
    return if (isNullityCheck && negations.size <= 1 && target != null) {
        VerboseNullabilityAndEmptinessNullCheck(
            check = negations.lastOrNull() ?: this,
            target = target,
            isPositive = (operationToken == KtTokens.EXCLEQ) xor negations.isNotEmpty(),
        )
    } else {
        null
    }
}
