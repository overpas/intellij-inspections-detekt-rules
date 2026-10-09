package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.startOffset

private const val MOVE_INTO_WHEN_RIGHT_MARGIN = 120

internal fun KtProperty.isMovableIntoWhen(whenExpression: KtWhenExpression): Boolean {
    val name = nameIdentifier?.text.orEmpty()
    val usages = whenExpression.moveIntoWhenUsages(name)
    val usagesAfter = (parent as? KtBlockExpression)?.statements.orEmpty()
        .asSequence()
        .dropWhile { it != this }
        .drop(1)
        .sumOf { it.moveIntoWhenUsages(name) }
    val start = whenExpression.startOffset
    val column = start - containingFile.text.lastIndexOf('\n', start - 1) - 1
    val isFitting = !textContains(
        '\n',
    ) && column + "when (".length + textLength + ") {".length <= MOVE_INTO_WHEN_RIGHT_MARGIN
    val hasSimpleInitializer = initializer?.isMoveIntoWhenInitializer() == true
    return !isVar && hasSimpleInitializer && usages > 1 && usages == usagesAfter && isFitting
}
