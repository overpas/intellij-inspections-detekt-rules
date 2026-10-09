package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExperimentalApi
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.resolution.KtResolvableCall

private val REPLACE_WITH_CALL_WITH_CONTEXT_CALL_WITH_ID = CallableId(FqName("kotlin"), Name.identifier("with"))

@OptIn(KaExperimentalApi::class, KtExperimentalApi::class)
internal class ReplaceWithCallWithContextCallUsages(
    session: KaSession,
    call: KtCallExpression,
    lambda: KtLambdaExpression,
) {

    private val receiver = with(session) { lambda.functionLiteral.symbol.receiverParameter }

    private val isWithCall = with(session) {
        call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } ==
            REPLACE_WITH_CALL_WITH_CONTEXT_CALL_WITH_ID
    }

    private val hasThisUsage = with(session) {
        lambda.bodyExpression
            ?.collectDescendantsOfType<KtThisExpression>()
            .orEmpty()
            .any { it.resolveSymbol() == receiver }
    }

    private val calls = with(session) {
        lambda.bodyExpression?.collectDescendantsOfType<KtSimpleNameExpression>().orEmpty()
            .mapNotNull { (it as? KtResolvableCall)?.resolveCall() as? KaSingleCall<*, *> }
    }

    fun usesReceiverOnlyAsContext(): Boolean =
        isWithCall &&
            receiver != null &&
            !hasThisUsage &&
            calls.none { it.dispatchReceiver.isReceiver() || it.extensionReceiver.isReceiver() } &&
            calls.any { single -> single.contextArguments.any { it.isReceiver() } }

    private fun KaReceiverValue?.isReceiver(): Boolean =
        (this as? KaImplicitReceiverValue)?.symbol == receiver
}
