package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.singleCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSuperExpression

internal fun KtSuperExpression.unqualifiedSuperSelector(selectorText: String): KtExpression? {
    val label = labelQualifier?.text.orEmpty()
    val fragment = KtPsiFactory(project).createExpressionCodeFragment("super$label.$selectorText", this)
    return (fragment.getContentElement() as? KtQualifiedExpression)?.selectorExpression
}

internal fun KtExpression.resolvedCallableId(): CallableId? =
    analyze(this) {
        resolveToCall()?.singleCallOrNull<KaCallableMemberCall<*, *>>()?.run { symbol.callableId }
    }
