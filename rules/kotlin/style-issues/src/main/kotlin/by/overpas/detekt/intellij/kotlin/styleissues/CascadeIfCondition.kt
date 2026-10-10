package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

internal class CascadeIfCondition(
    condition: KtExpression?,
    chainOperator: IElementType?,
) {

    private val binary = condition as? KtBinaryExpression

    private val operator = binary?.operationToken

    private val isChainable = (operator == KtTokens.OROR && chainOperator != KtTokens.ANDAND) ||
        (operator == KtTokens.ANDAND && chainOperator != KtTokens.OROR)

    val subject: KtExpression? = when {
        condition is KtIsExpression -> condition.leftHandSide

        operator == KtTokens.IN_KEYWORD || operator == KtTokens.NOT_IN -> binary?.left

        operator == KtTokens.EQEQ ->
            listOfNotNull(binary?.left, binary?.right).firstOrNull { it.isCascadeIfNameReference }

        isChainable -> binary?.let { CascadeIfOperands(it).subject }

        else -> null
    }?.takeIf { it is KtThisExpression || it.isCascadeIfNameReference }
}

internal val KtExpression.cascadeIfSubjectText: String
    get() = text.filterNot(Char::isWhitespace)

private val KtExpression.isCascadeIfNameReference: Boolean
    get() = this is KtNameReferenceExpression ||
        (this as? KtQualifiedExpression)?.selectorExpression is KtNameReferenceExpression
