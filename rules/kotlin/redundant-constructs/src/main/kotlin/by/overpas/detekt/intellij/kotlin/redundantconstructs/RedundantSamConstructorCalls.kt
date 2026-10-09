package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getPossiblyQualifiedCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

context(session: KaSession)
internal fun KtCallExpression.convertibleSamCalls(): Collection<KtCallExpression> {
    val resolved = with(session) { resolveToCall()?.successfulFunctionCallOrNull() } ?: return emptyList()
    val convertible = valueArguments
        .asSequence()
        .mapNotNull { argument -> argument.samConstructorCall()?.let { argument to it } }
        .filter { (argument, samCall) -> resolved.acceptsSamConversion(argument, samCall) }
        .filterNot { (_, samCall) -> samCall.hasBlockingLabeledReturn() }
        .toMap()
    return convertible.values.takeIf { it.isNotEmpty() && canDropSamConstructors(convertible) }.orEmpty()
}

internal fun KtValueArgument.samConstructorCall(): KtCallExpression? =
    getArgumentExpression()?.getPossiblyQualifiedCallExpression()

internal fun KtCallExpression.samLambdaArgument(): KtValueArgument? =
    valueArguments.singleOrNull()?.takeIf { it.getArgumentExpression() is KtLambdaExpression }

internal fun KtCallExpression.samCallContainer(): KtElement {
    val qualified = getQualifiedExpressionForSelectorOrThis()
    return qualified.parent as? KtDeclaration ?: qualified
}

internal fun KtCallExpression.resolvedFunctionIdentity(): Any? =
    analyze(this) {
        resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.psi ?: symbol.callableId }
    }
