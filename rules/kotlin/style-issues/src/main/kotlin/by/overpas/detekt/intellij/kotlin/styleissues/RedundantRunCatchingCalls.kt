package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

internal fun KtQualifiedExpression.redundantRunCatchingCandidate(): KtCallExpression? {
    val receiver = receiverExpression
    val first = (receiver as? KtQualifiedExpression)?.selectorExpression as? KtCallExpression
        ?: receiver as? KtCallExpression
    val second = selectorExpression as? KtCallExpression
    return first?.takeIf { call ->
        call.calleeExpression?.text == "runCatching" &&
            second?.calleeExpression?.text == "getOrThrow" &&
            call.lambdaArguments.firstOrNull()?.anyDescendantOfType<KtReturnExpression>() != true
    }
}

context(session: KaSession)
internal fun KtQualifiedExpression.isRedundantRunCatchingChain(runCatchingCall: KtCallExpression): Boolean {
    val secondCall = selectorExpression as? KtCallExpression
    return runCatchingCall.resolvedFqName() == "kotlin.runCatching" &&
        secondCall?.resolvedFqName() == "kotlin.getOrThrow"
}

context(session: KaSession)
private fun KtCallExpression.resolvedFqName(): String? =
    with(session) { resolveToCall()?.successfulFunctionCallOrNull() }
        ?.symbol
        ?.callableId
        ?.run { asSingleFqName().asString() }
