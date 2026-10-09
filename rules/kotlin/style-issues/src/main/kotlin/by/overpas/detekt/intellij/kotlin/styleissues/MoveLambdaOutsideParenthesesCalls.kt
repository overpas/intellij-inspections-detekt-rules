package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.unpackFunctionLiteral

context(session: KaSession)
internal fun KtCallExpression.canMoveLambdaOut(): Boolean {
    val call = with(session) { resolveToCall()?.successfulFunctionCallOrNull() }
    val lastParameter = call?.run { signature.valueParameters.lastOrNull()?.symbol }
    val mapping = call?.valueArgumentMapping.orEmpty()
    val argument = valueArguments.last().getArgumentExpression()
    val parameter = listOfNotNull(argument?.unpackFunctionLiteral(), argument)
        .firstNotNullOfOrNull { mapping[it] }
        ?.takeIf { !it.symbol.isVararg && it.symbol == lastParameter }
    val type = parameter?.returnType
    val isFunctional = type != null &&
        with(session) {
            type is KaTypeParameterType || type.isFunctionType || type.isSuspendFunctionType ||
                type.isFunctionalInterface
        }
    return calleeExpression !is KtNameReferenceExpression || isFunctional
}
