package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression

internal fun KtBinaryExpression.sizeZeroCheckWithIsEmptyTarget(): KtExpression? =
    when (operationToken) {
        KtTokens.EQEQ -> when {
            right.isSizeCheckConstant(0) -> left
            left.isSizeCheckConstant(0) -> right
            else -> null
        }

        KtTokens.GTEQ -> right.takeIf { left.isSizeCheckConstant(0) }

        KtTokens.GT -> right.takeIf { left.isSizeCheckConstant(1) }

        KtTokens.LTEQ -> left.takeIf { right.isSizeCheckConstant(0) }

        KtTokens.LT -> left.takeIf { right.isSizeCheckConstant(1) }

        else -> null
    }
