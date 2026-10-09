package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.StandardClassIds

private const val MANUAL_RANGE_SIZE = "size"

private const val MANUAL_RANGE_LAST_INDEX = "lastIndex"

private const val MANUAL_RANGE_INDICES = "indices"

private val MANUAL_RANGE_MEMBERS = setOf(MANUAL_RANGE_SIZE, MANUAL_RANGE_LAST_INDEX, MANUAL_RANGE_INDICES)

internal class ReplaceManualRangeWithIndicesCallsReceiver(
    session: KaSession,
    type: KaType,
) {

    private val memberTypes: Map<String, KaType> = with(session) {
        type.expandedSymbol?.run {
            memberScope.callables { name -> name.asString() in MANUAL_RANGE_MEMBERS }
                .filterIsInstance<KaVariableSymbol>()
                .associateBy({ it.name.asString() }, { it.returnType })
        }.orEmpty()
    }

    private val isArrayOrCollection = with(session) {
        type.isArrayOrPrimitiveArray || type.isSubtypeOf(StandardClassIds.Collection)
    }

    private val isCharSequence: Boolean = with(session) { type.isSubtypeOf(StandardClassIds.CharSequence) }

    private val hasSize: Boolean = isArrayOrCollection ||
        with(session) { memberTypes[MANUAL_RANGE_SIZE]?.isSubtypeOf(StandardClassIds.Int) == true }

    private val hasLastIndex: Boolean = isArrayOrCollection ||
        isCharSequence ||
        with(session) { memberTypes[MANUAL_RANGE_LAST_INDEX]?.isSubtypeOf(StandardClassIds.Int) == true }

    val hasIndices: Boolean = isArrayOrCollection ||
        isCharSequence ||
        with(session) { memberTypes[MANUAL_RANGE_INDICES]?.isSubtypeOf(StandardClassIds.IntRange) == true }

    fun isBound(
        selectorName: String?,
        isInclusive: Boolean,
    ): Boolean =
        when (selectorName) {
            MANUAL_RANGE_SIZE -> hasSize
            MANUAL_RANGE_LAST_INDEX -> isInclusive && hasLastIndex
            "length" -> isCharSequence
            else -> false
        }
}
