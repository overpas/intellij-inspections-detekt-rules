package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression

internal fun KtBinaryExpression.sizeCheckWithIsNotEmptyTarget(): KtExpression? =
    when (operationToken) {
        KtTokens.EXCLEQ -> when {
            right.isSizeCheckConstant(0) -> left
            left.isSizeCheckConstant(0) -> right
            else -> null
        }

        KtTokens.GTEQ -> left.takeIf { right.isSizeCheckConstant(1) }

        KtTokens.GT -> left.takeIf { right.isSizeCheckConstant(0) }

        KtTokens.LTEQ -> right.takeIf { left.isSizeCheckConstant(1) }

        KtTokens.LT -> right.takeIf { left.isSizeCheckConstant(0) }

        else -> null
    }
