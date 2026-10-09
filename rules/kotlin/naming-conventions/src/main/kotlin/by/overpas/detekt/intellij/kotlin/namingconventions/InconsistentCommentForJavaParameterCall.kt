package by.overpas.detekt.intellij.kotlin.namingconventions

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.fakeOverrideOriginal
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

private val javaOrigins = setOf(KaSymbolOrigin.JAVA_SOURCE, KaSymbolOrigin.JAVA_LIBRARY)

context(session: KaSession)
internal fun KtValueArgument.javaParameterExpectation(): InconsistentCommentForJavaParameterExpectation? =
    ((parent as? KtValueArgumentList)?.parent as? KtCallElement)
        ?.takeIf { call -> call.valueArguments.none { it.isNamed() } }
        ?.let { call -> with(session) { call.resolveToCall()?.successfulFunctionCallOrNull() } }
        ?.takeIf { call -> with(session) { call.symbol.fakeOverrideOriginal.origin } in javaOrigins }
        ?.namedParameterOf(this)
        ?.let { InconsistentCommentForJavaParameterExpectation(it) }

private fun KaFunctionCall<*>.namedParameterOf(argument: KtValueArgument): KaValueParameterSymbol? {
    val isAfterVararg = (argument.parent as? KtValueArgumentList)?.arguments.orEmpty()
        .asSequence()
        .takeWhile { it !== argument }
        .any { valueArgumentMapping[it.getArgumentExpression()]?.run { symbol.isVararg } == true }
    val parameterNames = symbol.valueParameters.map { it.name }
    return valueArgumentMapping[argument.getArgumentExpression()]?.run {
        symbol.takeUnless { parameter ->
            isAfterVararg || parameter.name.asString() == "p${parameterNames.indexOf(parameter.name)}"
        }
    }
}
