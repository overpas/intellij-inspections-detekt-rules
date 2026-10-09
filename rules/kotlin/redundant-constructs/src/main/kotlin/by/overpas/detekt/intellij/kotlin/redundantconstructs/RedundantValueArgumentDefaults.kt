package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtValueArgument

context(session: KaSession)
internal fun KtValueArgument.redundantDefaultParameterName(callElement: KtCallElement): String? {
    val call = with(session) { callElement.resolveToCall()?.successfulFunctionCallOrNull() } ?: return null
    val function = call.symbol
    val parameter = call.valueArgumentMapping[getArgumentExpression()]?.run { symbol.baseDeclaredParameter(function) }
    val following = callElement.valueArguments.dropWhile { it != this }.drop(1)
    val isRedundant = parameter != null &&
        matchesDefaultValueOf(parameter) &&
        following.all { it.isNamed() || !call.mapsToVarargParameter(it) }
    return parameter?.takeIf { isRedundant }?.run { name.asString() }
}
