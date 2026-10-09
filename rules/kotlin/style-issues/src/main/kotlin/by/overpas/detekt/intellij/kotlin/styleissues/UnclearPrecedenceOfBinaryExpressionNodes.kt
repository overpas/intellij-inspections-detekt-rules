package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtIsExpression

internal fun PsiElement.unclearPrecedenceNode(): UnclearPrecedenceOfBinaryExpressionNode? =
    when (this) {
        is KtBinaryExpression ->
            takeIf { operationToken !in KtTokens.ALL_ASSIGNMENTS && left != null && right != null }
                ?.let { UnclearPrecedenceOfBinaryExpressionNode(it, it.operationReference, listOf(it.left, it.right)) }

        is KtBinaryExpressionWithTypeRHS ->
            right?.let { UnclearPrecedenceOfBinaryExpressionNode(this, operationReference, listOf(left, it)) }

        is KtIsExpression ->
            typeReference?.let { type ->
                UnclearPrecedenceOfBinaryExpressionNode(this, operationReference, listOf(leftHandSide, type))
            }

        else -> null
    }
