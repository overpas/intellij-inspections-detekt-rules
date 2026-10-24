package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.psi.KtExpression

internal class BlockingMethodInNonBlockingContextDispatchers(
    private val call: KaFunctionCall<*>,
    private val callExpression: KtExpression,
) {

    context(session: KaSession)
    fun dispatcher(): BlockingMethodInNonBlockingContextDispatcher {
        val sources = sequence {
            yield(BlockingMethodInNonBlockingContextArgument(call).dispatcher())
            yield(BlockingMethodInNonBlockingContextScope(callExpression).dispatcher())
            yield(BlockingMethodInNonBlockingContextReceiver(callExpression).dispatcher())
            yield(BlockingMethodInNonBlockingContextInvoke(call).dispatcher())
            yield(BlockingMethodInNonBlockingContextFlowOn(call, callExpression).dispatcher())
        }
        val results = sources.runningFold(BlockingMethodInNonBlockingContextDispatcher.NO_INFORMATION, ::maxOf)
        return results.firstOrNull { it.isDefinite() } ?: results.last()
    }
}
