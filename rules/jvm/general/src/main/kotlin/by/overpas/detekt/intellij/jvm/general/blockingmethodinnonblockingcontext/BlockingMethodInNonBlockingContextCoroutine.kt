package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtPsiUtil

private val RESTRICTS_SUSPENSION = BlockingMethodInNonBlockingContextAnnotatedType(
    ClassId.fromString("kotlin/coroutines/RestrictsSuspension"),
    isSuperClassChecked = true,
)

internal class BlockingMethodInNonBlockingContextCoroutine(
    private val expression: KtCallExpression,
    isSuspendContextNonBlocking: Boolean,
    private val blockingLambdaFunctions: List<String>,
) {

    private val default = if (isSuspendContextNonBlocking) {
        BlockingMethodInNonBlockingContextType.NON_BLOCKING
    } else {
        BlockingMethodInNonBlockingContextType.UNSURE
    }

    context(session: KaSession)
    fun type(): BlockingMethodInNonBlockingContextType {
        val suspendContext = BlockingMethodInNonBlockingContextSuspendContext(expression).find()
        val lambda = (suspendContext as? KtFunctionLiteral)?.parent as? KtExpression
        val callExpression = suspendContext?.let { KtPsiUtil.getParentCallIfPresent(it) }
        val call = callExpression?.let { with(session) { it.resolveToCall()?.successfulFunctionCallOrNull() } }
        return when {
            suspendContext == null -> BlockingMethodInNonBlockingContextType.BLOCKING

            lambda == null || callExpression == null -> default

            call == null ||
                call.isRestrictedSuspend(lambda) ||
                call.symbol.callableId?.run { asSingleFqName().asString() } in blockingLambdaFunctions ->
                BlockingMethodInNonBlockingContextType.BLOCKING

            else -> BlockingMethodInNonBlockingContextDispatchers(call, callExpression).dispatcher().asType(default)
        }
    }

    context(session: KaSession)
    private fun KaFunctionCall<*>.isRestrictedSuspend(lambda: KtExpression): Boolean {
        val type = valueArgumentMapping[lambda]?.returnType as? KaFunctionType
        return type?.receiverType?.let { RESTRICTS_SUSPENSION.matches(it) } == true
    }
}
