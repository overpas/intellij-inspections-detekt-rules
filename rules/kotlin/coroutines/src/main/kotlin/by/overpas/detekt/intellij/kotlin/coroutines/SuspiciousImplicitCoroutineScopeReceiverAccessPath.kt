package by.overpas.detekt.intellij.kotlin.coroutines

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class SuspiciousImplicitCoroutineScopeReceiverAccessPath(
    private val session: KaSession,
    private val owner: KaSymbol,
) {

    fun isSuspendingFrom(expression: KtExpression): Boolean {
        val declaration = owner.psi
        return declaration != null &&
            expression.parents.takeWhile { it != declaration }.firstNotNullOfOrNull { it.suspendBoundary() } == true
    }

    private fun PsiElement.suspendBoundary(): Boolean? =
        when (this) {
            is KtFunctionLiteral ->
                if (with(session) { symbol } == owner) {
                    false
                } else {
                    SuspiciousImplicitCoroutineScopeReceiverAccessLambda(session, this).suspendBoundary()
                }

            is KtNamedFunction -> hasModifier(KtTokens.SUSPEND_KEYWORD).takeIf { it }

            else -> null
        }
}
