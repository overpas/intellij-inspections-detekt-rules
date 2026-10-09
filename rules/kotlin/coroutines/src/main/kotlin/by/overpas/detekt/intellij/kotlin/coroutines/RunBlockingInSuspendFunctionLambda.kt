package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

internal class RunBlockingInSuspendFunctionLambda(
    private val session: KaSession,
    private val function: KtFunction,
) {

    private val lambda: KtExpression = function.parent as? KtLambdaExpression ?: function

    fun expression(): KtExpression =
        lambda

    fun isInlined(): Boolean =
        with(session) {
            val isLambda = function is KtFunctionLiteral || (function is KtNamedFunction && function.isAnonymous)
            val argument = lambda.parent as? KtValueArgument
            val call = argument?.getStrictParentOfType<KtCallElement>()?.resolveToCall()?.successfulFunctionCallOrNull()
            val parameter = call?.valueArgumentMapping?.get(argument.getArgumentExpression())?.symbol
            val type = parameter?.returnType
            isLambda &&
                (call?.symbol as? KaNamedFunctionSymbol)?.isInline == true &&
                parameter?.isNoinline == false &&
                !parameter.isCrossinline &&
                type?.isMarkedNullable == false &&
                (type.isFunctionType || type.isSuspendFunctionType)
        }
}
