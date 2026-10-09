package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal class ConvertTwoComparisonsToRangeCheckComparison(
    binary: KtBinaryExpression,
    isInverted: Boolean,
) {

    private val token = binary.operationToken

    private val isLeftLesser = (token == KtTokens.LT || token == KtTokens.LTEQ) != isInverted

    private val left = binary.left?.let { KtPsiUtil.safeDeparenthesize(it, true) }

    private val right = binary.right?.let { KtPsiUtil.safeDeparenthesize(it, true) }

    val isValid: Boolean = left != null &&
        right != null &&
        token in setOf(KtTokens.LT, KtTokens.LTEQ, KtTokens.GT, KtTokens.GTEQ)

    val isStrict: Boolean = (token == KtTokens.LT || token == KtTokens.GT) != isInverted

    val lesser: KtExpression? = if (isLeftLesser) left else right

    val greater: KtExpression? = if (isLeftLesser) right else left

    fun isLowBoundFor(other: ConvertTwoComparisonsToRangeCheckComparison): Boolean =
        greater !is KtConstantExpression && greater?.text == other.lesser?.text
}
