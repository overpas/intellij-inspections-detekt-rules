package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private val COROUTINES_PACKAGE = FqName("kotlinx.coroutines")

private val RUN_BLOCKING_ID = CallableId(COROUTINES_PACKAGE, Name.identifier("runBlocking"))

private val COROUTINE_CONTEXT_CLASS_ID = ClassId(FqName("kotlin.coroutines"), Name.identifier("CoroutineContext"))

private val COROUTINE_SCOPE_CLASS_ID = ClassId(COROUTINES_PACKAGE, Name.identifier("CoroutineScope"))

internal class RunBlockingInSuspendFunctionCall(
    private val session: KaSession,
    private val expression: KtCallExpression,
) {

    fun isRunBlocking(): Boolean =
        with(session) {
            val function = expression.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val parameters = function?.valueParameters.orEmpty()
            val block = parameters.lastOrNull()?.returnType
            function?.callableId == RUN_BLOCKING_ID &&
                parameters.size == 2 &&
                parameters.first().returnType.symbol?.classId == COROUTINE_CONTEXT_CLASS_ID &&
                block?.isSuspendFunctionType == true &&
                (block as? KaFunctionType)?.receiverType?.symbol?.classId == COROUTINE_SCOPE_CLASS_ID
        }
}
