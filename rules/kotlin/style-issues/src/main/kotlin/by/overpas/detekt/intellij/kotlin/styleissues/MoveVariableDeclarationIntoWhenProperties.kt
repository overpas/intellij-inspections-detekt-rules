package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression

internal fun KtExpression.moveIntoWhenProperty(): KtProperty? =
    generateSequence(this) { it.moveIntoWhenWrapper() }
        .firstNotNullOfOrNull { expression ->
            val statements = (expression.parent as? KtBlockExpression)?.statements.orEmpty()
            statements.getOrNull(statements.indexOf(expression) - 1) as? KtProperty
        }

private fun KtExpression.moveIntoWhenWrapper(): KtExpression? {
    val wrapper = parent as? KtExpression
    val wrapped = when (wrapper) {
        is KtProperty -> wrapper.initializer
        is KtReturnExpression -> wrapper.returnedExpression
        is KtBinaryExpression -> wrapper.left
        is KtUnaryExpression -> wrapper.baseExpression
        else -> null
    }
    return wrapper?.takeIf { wrapped == this }
}
