package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

internal object ReplaceAddAllWithMapToSymbols {

    private val plusAssignId = CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier("plusAssign"))

    private val addAllId = CallableId(StandardClassIds.MutableCollection, Name.identifier("addAll"))

    fun isPlusAssign(symbol: KaFunctionSymbol): Boolean =
        symbol.callableId == plusAssignId &&
            (symbol.receiverParameter?.returnType as? KaClassType)?.classId == StandardClassIds.MutableCollection &&
            (symbol.valueParameters.singleOrNull()?.returnType as? KaClassType)?.classId == StandardClassIds.Iterable

    context(session: KaSession)
    fun isAddAll(call: KaFunctionCall<*>): Boolean {
        val receiverType = call.dispatchReceiver?.type ?: return isPlusAssign(call.symbol)
        return with(session) {
            receiverType.isSubtypeOf(StandardClassIds.MutableCollection) &&
                (sequenceOf(call.symbol) + call.symbol.allOverriddenSymbols).any { it.callableId == addAllId }
        }
    }
}
