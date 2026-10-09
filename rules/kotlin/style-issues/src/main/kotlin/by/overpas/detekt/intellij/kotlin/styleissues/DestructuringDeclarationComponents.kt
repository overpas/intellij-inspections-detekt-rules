package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.StandardClassIds

private val DESTRUCTURING_DECLARATION_MAP_ENTRY_COMPONENTS =
    mapOf("key" to 0, "value" to 1, "getKey" to 0, "getValue" to 1)

internal class DestructuringDeclarationComponents(private val type: KaType) {

    context(session: KaSession)
    fun indices(): Map<String, Int>? =
        with(session) {
            val classType = type.lowerBoundIfFlexible() as? KaClassType
            val symbol = classType?.takeUnless { it.isMarkedNullable }?.expandedSymbol
            val mapEntry = findClass(StandardClassIds.MapEntry)
            when {
                symbol is KaNamedClassSymbol && symbol.isData ->
                    symbol.declaredMemberScope.constructors
                        .firstOrNull { it.isPrimary }
                        ?.run { valueParameters.withIndex().associateBy({ it.value.name.asString() }, { it.index }) }

                symbol != null && mapEntry != null && (symbol == mapEntry || symbol.isSubClassOf(mapEntry)) ->
                    DESTRUCTURING_DECLARATION_MAP_ENTRY_COMPONENTS

                else -> null
            }
        }
}
