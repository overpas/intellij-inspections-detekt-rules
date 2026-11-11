package by.overpas.detekt.intellij.kotlin.coroutines.coroutinecontextwithjob

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private val COROUTINES_PACKAGE = FqName("kotlinx.coroutines")

private val COROUTINE_CONTEXT_CLASS_ID = ClassId(FqName("kotlin.coroutines"), Name.identifier("CoroutineContext"))

private val BUILDERS_ALLOWING_NON_CANCELLABLE: Map<CallableId, Boolean> = mapOf(
    CallableId(COROUTINES_PACKAGE, Name.identifier("launch")) to false,
    CallableId(COROUTINES_PACKAGE, Name.identifier("async")) to false,
    CallableId(COROUTINES_PACKAGE, Name.identifier("promise")) to false,
    CallableId(FqName("kotlinx.coroutines.future"), Name.identifier("future")) to false,
    CallableId(FqName("kotlinx.coroutines.channels"), Name.identifier("produce")) to false,
    CallableId(COROUTINES_PACKAGE, Name.identifier("withContext")) to true,
    CallableId(FqName("kotlinx.coroutines.flow"), Name.identifier("flowOn")) to true,
)

internal class CoroutineContextWithJobBuilderCall(
    private val session: KaSession,
    private val expression: KtCallExpression,
    private val additionalBuilders: Set<String>,
) {

    fun problem(): CoroutineContextWithJobProblem? =
        with(session) {
            val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
            val callableId = call?.run { symbol.callableId }
            val isNonCancellableAllowed = callableId?.let { id ->
                BUILDERS_ALLOWING_NON_CANCELLABLE[id]
                    ?: false.takeIf { id.asSingleFqName().asString() in additionalBuilders }
            }
            val builder = callableId?.takeIf { isNonCancellableAllowed != null }
            val job = call?.valueArgumentMapping?.entries
                ?.takeIf { builder != null }
                ?.firstOrNull { (_, parameter) -> parameter.returnType.isSubtypeOf(COROUTINE_CONTEXT_CLASS_ID) }
                ?.let { (argument, _) -> CoroutineContextWithJobDetector(session, argument).status() }
                as? CoroutineContextWithJobStatus.WithJob
            val builderName = builder?.run { callableName.asString() }.orEmpty()
            job
                ?.takeIf { it.isCancellable || isNonCancellableAllowed == false }
                ?.let { CoroutineContextWithJobProblem(it.source, it.messageFor(builderName)) }
        }
}
