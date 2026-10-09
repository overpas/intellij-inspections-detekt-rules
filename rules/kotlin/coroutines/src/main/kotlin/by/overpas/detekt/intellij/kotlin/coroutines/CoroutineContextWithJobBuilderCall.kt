package by.overpas.detekt.intellij.kotlin.coroutines

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
) {

    fun problem(): CoroutineContextWithJobProblem? =
        with(session) {
            val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
            val builder = call?.symbol?.callableId?.takeIf { it in BUILDERS_ALLOWING_NON_CANCELLABLE }
            val job = call?.valueArgumentMapping?.entries
                ?.takeIf { builder != null }
                ?.firstOrNull { (_, parameter) -> parameter.returnType.isSubtypeOf(COROUTINE_CONTEXT_CLASS_ID) }
                ?.let { (argument, _) -> CoroutineContextWithJobDetector(session, argument).status() }
                as? CoroutineContextWithJobStatus.WithJob
            val builderName = builder?.run { callableName.asString() }.orEmpty()
            job
                ?.takeIf { it.isCancellable || BUILDERS_ALLOWING_NON_CANCELLABLE[builder] == false }
                ?.let { CoroutineContextWithJobProblem(it.source, it.messageFor(builderName)) }
        }
}
