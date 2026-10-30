@file:OptIn(KaIdeApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs.removeredundantqualifiername

import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

internal fun KaSession.keepsCallTargetUnqualified(element: KtElement): Boolean {
    val selector = (element as? KtDotQualifiedExpression)?.selectorExpression ?: return true
    val fragment = KtPsiFactory(selector.project).createExpressionCodeFragment(selector.text, selector.parent)
    val target = callTarget(selector)
    return target == null || target == analyze(fragment) { fragment.getContentElement()?.let { callTarget(it) } }
}

private fun KaSession.callTarget(expression: KtExpression): FqName? =
    expression.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()?.run { symbol.importableFqName }
