package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBreakExpression
import org.jetbrains.kotlin.psi.KtContinueExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

private val FOLD_INITIALIZER_AND_IF_TO_ELVIS_COMPLEX_EXPRESSIONS = listOf(
    KtThrowExpression::class,
    KtReturnExpression::class,
    KtBreakExpression::class,
    KtContinueExpression::class,
    KtIfExpression::class,
    KtWhenExpression::class,
    KtTryExpression::class,
    KtLambdaExpression::class,
)

internal class FoldInitializerAndIfToElvisInitializer(private val initializer: KtExpression) {

    fun isSimple(): Boolean =
        '\n' !in initializer.text &&
            !initializer.anyDescendantOfType<KtExpression> { expression ->
                FOLD_INITIALIZER_AND_IF_TO_ELVIS_COMPLEX_EXPRESSIONS.any { it.isInstance(expression) } ||
                    (expression is KtBinaryExpression && expression.operationToken == KtTokens.ELVIS)
            }
}
