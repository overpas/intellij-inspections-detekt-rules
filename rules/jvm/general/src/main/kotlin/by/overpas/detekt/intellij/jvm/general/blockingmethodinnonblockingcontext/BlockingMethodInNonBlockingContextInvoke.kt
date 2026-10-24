package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName

private val DISPATCHER_INVOKE = FqName("kotlinx.coroutines.invoke")

internal class BlockingMethodInNonBlockingContextInvoke(private val call: KaFunctionCall<*>) {

    context(session: KaSession)
    fun dispatcher(): BlockingMethodInNonBlockingContextDispatcher {
        val isInvoke = call.symbol.callableId?.asSingleFqName() == DISPATCHER_INVOKE
        val receiver = (call.extensionReceiver as? KaExplicitReceiverValue)?.expression
        return if (isInvoke && receiver != null) {
            BlockingMethodInNonBlockingContextNested(receiver).dispatcher()
        } else {
            BlockingMethodInNonBlockingContextDispatcher.NO_INFORMATION
        }
    }
}
