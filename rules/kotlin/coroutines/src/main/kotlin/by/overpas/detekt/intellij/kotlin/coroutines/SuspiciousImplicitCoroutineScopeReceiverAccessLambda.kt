package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class SuspiciousImplicitCoroutineScopeReceiverAccessLambda(
    private val session: KaSession,
    literal: KtFunctionLiteral,
) {

    private val argument = literal.parents
        .dropWhile { it is KtLambdaExpression || it is KtLabeledExpression || it is KtParenthesizedExpression }
        .firstOrNull() as? KtValueArgument

    private val call = with(session) {
        val argumentParent = argument?.parent
        val callExpression = ((argumentParent as? KtValueArgumentList)?.parent ?: argumentParent) as? KtCallExpression
        callExpression?.resolveToCall()?.successfulFunctionCallOrNull()
    }

    private val parameter = call?.valueArgumentMapping?.get(argument?.getArgumentExpression())?.symbol

    fun suspendBoundary(): Boolean? =
        with(session) {
            val function = call?.symbol
            val parameterType = parameter?.returnType
            val isInlined = (function as? KaNamedFunctionSymbol)?.isInline == true &&
                parameter?.isNoinline == false &&
                !parameter.isCrossinline &&
                parameterType?.isMarkedNullable == false &&
                (parameterType.isFunctionType || parameterType.isSuspendFunctionType)
            val lambdaType = if (parameterType?.isFunctionalInterface == true) {
                parameterType.symbol
                    ?.samConstructor
                    ?.valueParameters
                    ?.singleOrNull()
                    ?.returnType
            } else {
                parameterType
            }
            when {
                isInlined -> null
                lambdaType == null -> false
                lambdaType.isSuspendFunctionType && function?.callableId !in SAFE_SUSPENDING_FUNCTIONS -> true
                else -> null
            }
        }

    private companion object {
        val SELECTS_PACKAGE = FqName("kotlinx.coroutines.selects")
        val SAFE_SUSPENDING_FUNCTIONS = setOf(
            CallableId(ClassId(SELECTS_PACKAGE, Name.identifier("SelectBuilder")), Name.identifier("invoke")),
            CallableId(SELECTS_PACKAGE, Name.identifier("onTimeout")),
        )
    }
}
