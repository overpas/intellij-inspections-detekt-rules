package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.types.Variance

internal class AddVarianceModifierUsages(
    private val session: KaSession,
    private val typeParameter: KaTypeParameterSymbol,
) {

    fun suggestedVariance(): Variance? =
        with(session) {
            val owner = typeParameter.containingSymbol as? KaNamedClassSymbol
            val scope = owner?.declaredMemberScope
            val members = scope?.callables.orEmpty()
                .filter { it.visibility == KaSymbolVisibility.PUBLIC || it.visibility == KaSymbolVisibility.INTERNAL }
                .toList()
            val properties = members.filterIsInstance<KaPropertySymbol>().filter {
                it.returnType.mentionsTypeParameter()
            }
            val functions = members.filterIsInstance<KaFunctionSymbol>()
            val parameters = scope?.constructors?.firstOrNull { it.isPrimary }?.valueParameters.orEmpty() +
                functions.flatMap { it.valueParameters }
            val isContravariant = parameters.any { it.returnType.mentionsTypeParameter() } ||
                properties.any { it.setter != null }
            val isCovariant = properties.isNotEmpty() || functions.any { it.returnType.mentionsTypeParameter() }
            when {
                owner == null || typeParameter.upperBounds.any { it.mentionsTypeParameter() } -> null
                isContravariant == isCovariant -> null
                isContravariant -> Variance.IN_VARIANCE
                else -> Variance.OUT_VARIANCE
            }
        }

    private fun KaType.mentionsTypeParameter(): Boolean =
        generateSequence(listOf(this)) { types ->
            types.flatMap { type -> (type as? KaClassType)?.run { typeArguments.mapNotNull { it.type } }.orEmpty() }
                .takeIf { it.isNotEmpty() }
        }
            .flatten()
            .any { it is KaTypeParameterType && it.symbol == typeParameter }
}
