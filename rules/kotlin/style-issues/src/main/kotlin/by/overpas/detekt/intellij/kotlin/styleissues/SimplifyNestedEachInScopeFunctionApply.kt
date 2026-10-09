package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

context(session: KaSession)
internal fun SimplifyNestedEachInScopeFunctionCall.isSimplifiableApply(): Boolean =
    with(session) {
        val scopeReceiver = scopeCall.nestedEachReceiverType()
        val eachReceiver = eachCall?.nestedEachReceiverType()
        val qualifier = (statement as? KtDotQualifiedExpression)?.receiverExpression
        val thisLabel = (qualifier as? KtThisExpression)?.getLabelName()
        val owner = lambda.functionLiteral.symbol
        scopeReceiver != null &&
            eachReceiver != null &&
            scopeReceiver.isSubtypeOf(eachReceiver) &&
            (qualifier == null || qualifier is KtThisExpression) &&
            (thisLabel == null || thisLabel == labelName) &&
            eachLambdaBody?.anyDescendantOfType<KtElement> { it.usesNestedEachReceiverOf(owner) } != true
    }

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
private fun KtCallExpression.nestedEachReceiverType(): KaType? =
    with(session) {
        val call = resolveToCall()?.successfulFunctionCallOrNull()
        (call?.dispatchReceiver ?: call?.extensionReceiver)?.type
    }
