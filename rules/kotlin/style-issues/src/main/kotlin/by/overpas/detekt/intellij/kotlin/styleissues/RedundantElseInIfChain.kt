package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression

internal class RedundantElseInIfChain(private val expression: KtIfExpression) {

    private val branches = generateSequence(expression) { it.`else` as? KtIfExpression }

    val isElseBranch: Boolean
        get() = expression.parent.node.elementType == KtNodeTypes.ELSE

    val lastElseKeyword: PsiElement?
        get() = branches.last().elseKeyword

    context(session: KaSession)
    fun isRedundant(): Boolean =
        with(session) {
            !expression.isUsedAsExpression &&
                branches.all { branch ->
                    val then = branch.then
                    val last = (then as? KtBlockExpression)?.run { statements.lastOrNull() } ?: then
                    last?.expressionType?.isNothingType == true
                }
        }
}
