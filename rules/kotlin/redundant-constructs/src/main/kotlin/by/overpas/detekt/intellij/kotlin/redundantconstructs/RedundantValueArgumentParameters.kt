package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.ValueArgument

context(session: KaSession)
internal fun KtValueArgument.matchesDefaultValueOf(parameter: KaValueParameterSymbol): Boolean =
    with(session) {
        val defaultValue = (parameter.psi as? KtParameter)?.takeIf { !it.isVarArg }?.defaultValue?.evaluate()
        val value = getArgumentExpression()?.evaluate()
        defaultValue != null && value != null && defaultValue.value == value.value
    }

internal fun KaFunctionCall<*>.mapsToVarargParameter(argument: ValueArgument): Boolean {
    val expression = argument.getArgumentExpression() ?: return true
    return valueArgumentMapping[expression]?.run { symbol.isVararg } != false
}

context(session: KaSession)
internal fun KaValueParameterSymbol.baseDeclaredParameter(function: KaFunctionSymbol): KaValueParameterSymbol? {
    if (function !is KaNamedFunctionSymbol || !function.isOverride) return this
    val base = with(session) { function.allOverriddenSymbols }
        .firstOrNull { it is KaNamedFunctionSymbol && !it.isOverride }
    val parameterName = name
    return (base as? KaNamedFunctionSymbol)?.run { valueParameters.singleOrNull { it.name == parameterName } }
}
