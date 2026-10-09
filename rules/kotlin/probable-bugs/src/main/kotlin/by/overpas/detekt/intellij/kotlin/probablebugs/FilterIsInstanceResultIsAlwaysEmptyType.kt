package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.analysis.api.types.symbol

internal class FilterIsInstanceResultIsAlwaysEmptyType(type: KaType) {

    private val bounds = when (type) {
        is KaTypeParameterType -> type.symbol.upperBounds
        is KaClassType -> listOf(type)
        else -> emptyList()
    }

    private val classBound = bounds.singleOrNull {
        (it.symbol as? KaNamedClassSymbol)?.classKind != KaClassKind.INTERFACE
    }

    private val finalClassBound = classBound?.takeIf { it.symbol?.modality == KaSymbolModality.FINAL }

    context(session: KaSession)
    fun isDisjointFrom(other: FilterIsInstanceResultIsAlwaysEmptyType): Boolean =
        with(session) {
            val isOwnFinalOutside = finalClassBound
                ?.let { finalBound -> other.bounds.any { !finalBound.isSubtypeOf(it) } } == true
            val isOtherFinalOutside = other.finalClassBound
                ?.let { finalBound -> bounds.any { !finalBound.isSubtypeOf(it) } } == true
            val areUnrelated = listOfNotNull(classBound, other.classBound)
                .takeIf { it.size == 2 }
                ?.let { (own, another) -> !own.isSubtypeOf(another) && !another.isSubtypeOf(own) } == true
            isOwnFinalOutside || isOtherFinalOutside || areUnrelated
        }
}
