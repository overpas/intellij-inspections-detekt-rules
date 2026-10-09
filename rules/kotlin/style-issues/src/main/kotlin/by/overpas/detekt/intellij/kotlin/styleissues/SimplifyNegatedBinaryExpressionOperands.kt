package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.parents

internal fun KtPrefixExpression.isSimplifiableNegation(): Boolean {
    val operand = KtPsiUtil.deparenthesize(baseExpression)
    val enclosing = parents.firstOrNull { it !is KtParenthesizedExpression } as? KtPrefixExpression
    val isBooleanLiteral = operand is KtConstantExpression && operand.node.elementType == KtNodeTypes.BOOLEAN_CONSTANT
    return operationToken == KtTokens.EXCL &&
        enclosing?.operationToken != KtTokens.EXCL &&
        (isBooleanLiteral || operand.isInvertibleOperation())
}
