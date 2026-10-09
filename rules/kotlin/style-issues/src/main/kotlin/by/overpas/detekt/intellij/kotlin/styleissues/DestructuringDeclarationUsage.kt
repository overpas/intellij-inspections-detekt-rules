package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver

internal class DestructuringDeclarationUsage(
    reference: KtNameReferenceExpression,
    private val components: Map<String, Int>,
) {

    private val destructuring = (reference.parent as? KtDestructuringDeclaration)
        ?.takeIf { it.initializer == reference }

    private val qualified = reference.getQualifiedExpressionForReceiver()

    private val property = qualified?.parent as? KtProperty

    fun isDestructuring(): Boolean =
        destructuring != null

    fun dropsStatement(): Boolean =
        destructuring != null || property != null

    fun claimedIndices(): List<Int>? =
        when {
            destructuring != null ->
                destructuring.entries
                    .take(components.values.toSet().size)
                    .takeIf { entries -> entries.all { it.name != null } }
                    ?.run { indices.toList() }

            isModified() -> null

            else -> selectorName()?.let { components[it] }?.let { listOf(it) }
        }

    private fun isModified(): Boolean {
        val parent = qualified?.parent
        val isAssigned = parent is KtBinaryExpression &&
            parent.operationToken in KtTokens.ALL_ASSIGNMENTS &&
            parent.left == qualified
        val isIncremented = parent is KtUnaryExpression && parent.operationToken in KtTokens.INCREMENT_AND_DECREMENT
        return qualified == null || property?.isVar == true || isAssigned || isIncremented
    }

    private fun selectorName(): String? =
        when (val selector = qualified?.selectorExpression) {
            is KtNameReferenceExpression -> selector.getReferencedName()

            is KtCallExpression ->
                (selector.calleeExpression as? KtNameReferenceExpression)
                    ?.takeIf { selector.valueArguments.isEmpty() && selector.lambdaArguments.isEmpty() }
                    ?.getReferencedName()

            else -> null
        }
}
