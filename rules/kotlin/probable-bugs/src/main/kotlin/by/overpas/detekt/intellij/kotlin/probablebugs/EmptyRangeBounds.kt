package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

private const val EMPTY_RANGE_MESSAGE = "This range is empty."

private const val EMPTY_RANGE_MAX_REFERENCE_DEPTH = 16

internal class EmptyRangeBounds(
    session: KaSession,
    range: KtExpression,
) {

    private val arguments = when (range) {
        is KtBinaryExpression -> listOf(range.left, range.right)

        is KtDotQualifiedExpression -> listOf(
            range.receiverExpression,
            (range.selectorExpression as? KtCallExpression)?.valueArguments?.singleOrNull()?.getArgumentExpression(),
        )

        else -> emptyList()
    }

    private val values = with(session) {
        arguments.map { argument ->
            val source = generateSequence(argument) { current ->
                val references = (current as? KtNameReferenceExpression)?.references.orEmpty()
                val target = references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
                (target?.psi as? KtProperty)?.takeUnless { it.isVar }?.initializer
            }.take(EMPTY_RANGE_MAX_REFERENCE_DEPTH).lastOrNull()
            source?.takeUnless { it is KtNameReferenceExpression }?.evaluate()?.value
        }
    }

    val comparison: Int? = values
        .mapNotNull { if (it is Number && it !is Double && it !is Float) it.toLong() else it as? Comparable<*> }
        .takeIf { it.size == 2 && it.first()::class == it.last()::class }
        ?.let { compareValues(it.first(), it.last()) }
}

internal fun KtExpression.emptyRangeMessage(operator: EmptyRangeOperator): String? {
    val range = this
    return analyze(range) {
        val comparison = EmptyRangeBounds(this, range).comparison
        val suggestion = comparison?.let { operator.suggestionFor(it) }
        val isIterable = range.expressionType?.isSubtypeOf(StandardClassIds.Iterable) == true
        when {
            suggestion == null -> null
            isIterable || comparison == 0 -> "$EMPTY_RANGE_MESSAGE Did you mean to use '$suggestion'?"
            else -> EMPTY_RANGE_MESSAGE
        }
    }
}
