package by.overpas.detekt.intellij.kotlin.styleissues.addvariancemodifier

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.types.Variance

private val ADD_VARIANCE_MODIFIER_VISIBILITIES =
    setOf(KaSymbolVisibility.PUBLIC, KaSymbolVisibility.INTERNAL, KaSymbolVisibility.PROTECTED)

internal class AddVarianceModifierUsages(
    private val session: KaSession,
    private val typeParameter: KaTypeParameterSymbol,
) {

    fun suggestedVariance(): Variance? =
        with(session) {
            val positions = AddVarianceModifierPositions(typeParameter)
            val owner = typeParameter.containingSymbol as? KaNamedClassSymbol
            val scope = owner?.declaredMemberScope
            val members = scope?.callables.orEmpty()
                .filter { it.visibility in ADD_VARIANCE_MODIFIER_VISIBILITIES }
                .toList()
            val properties = members.filterIsInstance<KaPropertySymbol>()
            val functions = members.filterIsInstance<KaFunctionSymbol>()
            val constructorParameters = scope?.constructors?.firstOrNull { it.isPrimary }?.valueParameters.orEmpty()
                .filter { it.generatedPrimaryConstructorProperty?.visibility !in ADD_VARIANCE_MODIFIER_VISIBILITIES }
            val parameters = constructorParameters + functions.flatMap { it.valueParameters }
            val readOnly = properties.filter { it.setter == null }
            val mutable = properties.filter { it.setter != null }
            val uses = buildList {
                owner?.superTypes.orEmpty().forEach { addAll(positions.inType(it, Variance.OUT_VARIANCE)) }
                readOnly.forEach { addAll(positions.inType(it.returnType, Variance.OUT_VARIANCE)) }
                mutable.forEach { addAll(positions.inType(it.returnType, Variance.INVARIANT)) }
                functions.forEach { addAll(positions.inType(it.returnType, Variance.OUT_VARIANCE)) }
                parameters.forEach { addAll(positions.inType(it.returnType, Variance.IN_VARIANCE)) }
                members.forEach { member ->
                    member.receiverType?.let { addAll(positions.inType(it, Variance.IN_VARIANCE)) }
                }
            }
            val isBounded = typeParameter.upperBounds.any { positions.inType(it, Variance.OUT_VARIANCE).isNotEmpty() }
            when {
                owner == null || isBounded -> null
                uses.distinct().size != 1 || uses.first() == Variance.INVARIANT -> null
                else -> uses.first()
            }
        }
}
