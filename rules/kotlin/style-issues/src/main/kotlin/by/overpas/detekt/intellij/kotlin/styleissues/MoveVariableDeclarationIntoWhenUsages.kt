package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBreakExpression
import org.jetbrains.kotlin.psi.KtContinueExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private val MOVE_INTO_WHEN_COMPLEX_EXPRESSIONS = listOf(
    KtThrowExpression::class,
    KtReturnExpression::class,
    KtBreakExpression::class,
    KtContinueExpression::class,
    KtIfExpression::class,
    KtWhenExpression::class,
    KtTryExpression::class,
    KtLambdaExpression::class,
)

internal fun KtElement.moveIntoWhenUsages(name: String): Int =
    collectDescendantsOfType<KtReferenceExpression> { it.text == name }.size

internal fun KtExpression.isMoveIntoWhenInitializer(): Boolean =
    !textContains('\n') &&
        !anyDescendantOfType<KtExpression> { expression ->
            val isElvis = expression is KtBinaryExpression && expression.operationToken == KtTokens.ELVIS
            isElvis || MOVE_INTO_WHEN_COMPLEX_EXPRESSIONS.any { it.isInstance(expression) }
        }
