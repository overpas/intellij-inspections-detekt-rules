package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtTypeReference

context(session: KaSession)
internal fun KaFunctionCall<*>.simplifiableCallFlattens(): Boolean =
    with(session) {
        val receiverType = (dispatchReceiver ?: extensionReceiver)?.type as? KaClassType
        val elementType = receiverType?.typeArguments?.singleOrNull()?.type
        when {
            receiverType == null || elementType == null -> false

            receiverType.classId == StandardClassIds.Array -> (elementType as? KaClassType)?.classId ==
                StandardClassIds.Array

            else -> elementType.isSubtypeOf(StandardClassIds.Iterable)
        }
    }

context(session: KaSession)
internal fun KaFunctionCall<*>.simplifiableCallAccepts(typeReference: KtTypeReference): Boolean =
    with(session) {
        val elementType = (signature.returnType as? KaClassType)?.typeArguments?.singleOrNull()?.type
        elementType == null || typeReference.type.isSubtypeOf(elementType)
    }
