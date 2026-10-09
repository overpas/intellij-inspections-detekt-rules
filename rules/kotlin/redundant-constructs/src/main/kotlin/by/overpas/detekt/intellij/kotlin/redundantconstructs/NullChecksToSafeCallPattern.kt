package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.lexer.KtSingleValueToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private const val SAFE_CALL = "?."

private const val CALL = "."

internal fun KtBinaryExpression.nullCheckOperation(): KtSingleValueToken? =
    when (operationToken) {
        KtTokens.ANDAND -> KtTokens.EXCLEQ
        KtTokens.OROR -> KtTokens.EQEQ
        else -> null
    }

internal fun KtBinaryExpression.nullComparedExpression(expectedOperation: KtSingleValueToken): KtExpression? {
    val lhs = left
    val rhs = right
    return when {
        operationToken != expectedOperation || lhs == null || rhs == null -> null
        KtPsiUtil.isNullConstant(lhs) -> rhs
        KtPsiUtil.isNullConstant(rhs) -> lhs
        else -> null
    }
}

internal fun KtExpression.hasSameTextIgnoringSafeCalls(other: KtExpression): Boolean =
    text.replace(SAFE_CALL, CALL) == other.text.replace(SAFE_CALL, CALL)
