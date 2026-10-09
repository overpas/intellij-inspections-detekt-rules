package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaStarTypeProjection
import org.jetbrains.kotlin.analysis.api.types.KaType

internal class SimplifiableFlowCallFilter(
    private val session: KaSession,
    private val call: KaFunctionCall<*>,
    private val predicate: SimplifiableFlowCallLambda,
) {

    fun replacement(): String? {
        val type = predicate.isInstanceCheckType()
        return when {
            predicate.isNotNullCheck() -> "filterNotNull()"
            type != null && with(session) { isSubtypeOfElement(type.type) } -> "filterIsInstance<${type.text}>()"
            else -> null
        }
    }

    private fun KaSession.isSubtypeOfElement(type: KaType): Boolean {
        val element = (call.signature.returnType as? KaClassType)
            ?.typeArguments
            ?.singleOrNull()
            ?.takeIf { it !is KaStarTypeProjection }
            ?.type
        return element == null || type.isSubtypeOf(element)
    }
}
