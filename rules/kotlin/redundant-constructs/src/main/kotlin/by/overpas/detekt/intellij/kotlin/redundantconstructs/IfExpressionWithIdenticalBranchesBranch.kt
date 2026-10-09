package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.startOffset

internal class IfExpressionWithIdenticalBranchesBranch(branch: KtExpression) {

    private val expression: KtExpression = run {
        val inner = KtPsiUtil.safeDeparenthesize(branch, true)
        val statement = (inner as? KtBlockExpression)?.statements?.singleOrNull()
            ?.let { KtPsiUtil.safeDeparenthesize(it, true) }
        when {
            inner !is KtBlockExpression -> inner
            statement == null || statement is KtLambdaExpression -> branch
            else -> statement
        }
    }

    private val names: List<KtSimpleNameExpression>
        get() = listOfNotNull(expression as? KtSimpleNameExpression) +
            expression.collectDescendantsOfType<KtSimpleNameExpression>()

    fun tokens(): List<String> =
        PsiTreeUtil.collectElements(expression) { it.firstChild == null && it !is PsiWhiteSpace && it !is PsiComment }
            .map { it.text }

    fun resolvesLike(other: IfExpressionWithIdenticalBranchesBranch): Boolean =
        names.size == other.names.size &&
            analyze(expression) {
                val (targets, otherTargets) = listOf(names, other.names).map { branchNames ->
                    branchNames.map { name ->
                        name.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
                    }
                }
                targets.zip(otherTargets).all { (target, otherTarget) ->
                    target == otherTarget ||
                        offsetOf(target?.psi)?.let { it == other.offsetOf(otherTarget?.psi) } == true
                }
            }

    private fun offsetOf(target: PsiElement?): Int? =
        target?.takeIf { expression.isAncestor(it) }?.let { it.startOffset - expression.startOffset }
}
