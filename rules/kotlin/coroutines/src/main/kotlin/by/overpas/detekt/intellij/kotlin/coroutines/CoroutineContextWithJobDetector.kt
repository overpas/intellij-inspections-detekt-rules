package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private val COROUTINES_PACKAGE = FqName("kotlinx.coroutines")

private val STDLIB_COROUTINES_PACKAGE = FqName("kotlin.coroutines")

private val COROUTINE_CONTEXT_CLASS_ID = ClassId(STDLIB_COROUTINES_PACKAGE, Name.identifier("CoroutineContext"))

private val JOB_CLASS_ID = ClassId(COROUTINES_PACKAGE, Name.identifier("Job"))

private val NON_CANCELLABLE_CLASS_ID = ClassId(COROUTINES_PACKAGE, Name.identifier("NonCancellable"))

private val COROUTINE_DISPATCHER_CLASS_ID = ClassId(COROUTINES_PACKAGE, Name.identifier("CoroutineDispatcher"))

private val CONTEXT_PLUS_ID = CallableId(COROUTINE_CONTEXT_CLASS_ID, Name.identifier("plus"))

private val CONTEXT_MINUS_KEY_ID = CallableId(COROUTINE_CONTEXT_CLASS_ID, Name.identifier("minusKey"))

private val JOB_CONTEXT_SOURCES = setOf(
    CallableId(COROUTINES_PACKAGE, Name.identifier("currentCoroutineContext")),
    CallableId(STDLIB_COROUTINES_PACKAGE, Name.identifier("coroutineContext")),
    CallableId(ClassId(COROUTINES_PACKAGE, Name.identifier("CoroutineScope")), Name.identifier("coroutineContext")),
)

internal class CoroutineContextWithJobDetector(
    private val session: KaSession,
    expression: KtExpression,
) {

    private val expression: KtExpression = KtPsiUtil.safeDeparenthesize(expression)

    fun status(): CoroutineContextWithJobStatus =
        with(session) {
            val type = expression.expressionType
            val call = expression.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()
            val symbol = call?.symbol
            when {
                type == null -> CoroutineContextWithJobStatus.Unknown

                type.isSubtypeOf(NON_CANCELLABLE_CLASS_ID) -> CoroutineContextWithJobStatus.WithJob(expression, false)

                type.isSubtypeOf(JOB_CLASS_ID) -> CoroutineContextWithJobStatus.WithJob(expression, true)

                type.isSubtypeOf(COROUTINE_DISPATCHER_CLASS_ID) -> CoroutineContextWithJobStatus.NoJob

                type.isSubtypeOf(COROUTINE_CONTEXT_CLASS_ID) &&
                    (type.symbol as? KaClassSymbol)?.modality == KaSymbolModality.FINAL ->
                    CoroutineContextWithJobStatus.NoJob

                JOB_CONTEXT_SOURCES.any { symbol?.hasOrOverrides(it) == true } ->
                    CoroutineContextWithJobStatus.WithJob(expression, true)

                call is KaFunctionCall<*> && call.symbol.hasOrOverrides(CONTEXT_PLUS_ID) ->
                    CoroutineContextWithJobOperation(session, call).plusStatus()

                call is KaFunctionCall<*> && call.symbol.hasOrOverrides(CONTEXT_MINUS_KEY_ID) ->
                    CoroutineContextWithJobOperation(session, call).minusKeyStatus()

                else -> CoroutineContextWithJobStatus.Unknown
            }
        }

    private fun KaCallableSymbol.hasOrOverrides(callableId: CallableId): Boolean =
        (listOf(this) + with(session) { allOverriddenSymbols }).any { it.callableId == callableId }
}
