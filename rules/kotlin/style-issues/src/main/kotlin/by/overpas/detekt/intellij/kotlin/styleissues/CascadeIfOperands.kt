package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal class CascadeIfOperands(binary: KtBinaryExpression) {

    private val operator = binary.operationToken

    private val left = CascadeIfCondition(binary.left?.let { KtPsiUtil.safeDeparenthesize(it) }, operator).subject

    private val right = CascadeIfCondition(binary.right?.let { KtPsiUtil.safeDeparenthesize(it) }, operator).subject

    private val isConjunction = operator == KtTokens.ANDAND

    val subject: KtExpression? =
        left?.takeIf { isConjunction || it.cascadeIfSubjectText == right?.cascadeIfSubjectText }
}
