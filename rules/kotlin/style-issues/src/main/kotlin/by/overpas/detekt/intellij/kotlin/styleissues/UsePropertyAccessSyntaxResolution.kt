package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType

context(session: KaSession)
internal fun UsePropertyAccessSyntaxAccessor.syntheticProperty(): UsePropertyAccessSyntaxProperty? =
    with(session) {
        val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
        val receiverType = resolved?.dispatchReceiver?.run { type.lowerBoundIfFlexible() }
        val target = if (resolved == null || receiverType == null) {
            null
        } else {
            UsePropertyAccessSyntaxTarget(this@syntheticProperty, resolved.symbol, receiverType)
        }
        val receiver = (receiverType as? KaClassType)?.classId
        target?.convertibleProperty()?.let { property ->
            UsePropertyAccessSyntaxProperty(property.name.asString(), call, receiver)
        }
    }
