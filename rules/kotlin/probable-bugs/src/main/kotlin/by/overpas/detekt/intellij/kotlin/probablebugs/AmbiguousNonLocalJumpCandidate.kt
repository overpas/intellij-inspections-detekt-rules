package by.overpas.detekt.intellij.kotlin.probablebugs

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.psi.KtDoWhileExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtWhileExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class AmbiguousNonLocalJumpCandidate(private val jump: KtExpressionWithLabel) {

    private val loop = jump.parents
        .filterIsInstance<KtLoopExpression>()
        .firstOrNull()
        .takeIf { jump.getLabelName() == null }

    private val loopKeyword = when (loop) {
        is KtWhileExpression -> "while"
        is KtForExpression -> "for"
        is KtDoWhileExpression -> "do-while"
        else -> null
    }

    fun message(): String? {
        val keyword = loopKeyword
        val callee = keyword?.run {
            jump.parents.takeWhile { it !is KtLoopExpression }.firstNotNullOfOrNull { it.inlinedCallee() }
        }
        return callee?.run { "Ambiguous non-local '${jump.text}' ('$keyword' vs '$name'). $advice" }
    }

    private fun PsiElement.inlinedCallee(): AmbiguousNonLocalJumpCallee? {
        val call = ambiguousNonLocalJumpCall()
        val argument = this as? KtExpression
        return if (call == null || argument == null) {
            null
        } else {
            analyze(call) {
                val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
                val parameter = resolved?.run { valueArgumentMapping[argument]?.symbol }
                    ?.takeUnless { it.isCrossinline || it.isNoinline }
                val function = (resolved?.symbol as? KaNamedFunctionSymbol)?.takeIf { it.isInline }
                val isAmbiguous = parameter != null && function?.hasNoCallsInPlaceContract(parameter.name) == true
                AmbiguousNonLocalJumpCallee(
                    name = call.calleeExpression?.text.orEmpty(),
                    isInSource = function?.origin == KaSymbolOrigin.SOURCE,
                ).takeIf { isAmbiguous }
            }
        }
    }
}
