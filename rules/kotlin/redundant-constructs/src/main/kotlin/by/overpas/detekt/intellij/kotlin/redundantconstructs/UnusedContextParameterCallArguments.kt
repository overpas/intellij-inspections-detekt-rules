package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaLocalVariableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private val CONTEXT_CALLABLE_ID = CallableId(FqName("kotlin"), Name.identifier("context"))

@OptIn(KaExperimentalApi::class)
internal class UnusedContextParameterCallArguments(
    private val session: KaSession,
    private val call: KtCallExpression,
    private val lambda: KtLambdaExpression,
) {

    private val arguments = call.valueArguments.mapNotNull { it.getArgumentExpression() }.filter { it != lambda }

    private val parameters = with(session) { lambda.functionLiteral.symbol.contextParameters }

    fun targetsKotlinContext(): Boolean =
        with(session) {
            call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } == CONTEXT_CALLABLE_ID
        }

    fun matchParameters(): Boolean =
        parameters.isNotEmpty() && parameters.size == arguments.size && lambda.bodyExpression != null

    fun areSideEffectFree(): Boolean =
        arguments.all { it.isSideEffectFree() }

    fun hasUnused(): Boolean {
        val used = lambda.bodyExpression
            ?.collectDescendantsOfType<KtSimpleNameExpression>()
            .orEmpty()
            .flatMap { it.usedSymbols() }
            .toSet()
        return !used.containsAll(parameters)
    }

    private fun KtExpression.isSideEffectFree(): Boolean {
        val unwrapped = KtPsiUtil.deparenthesize(this)
        return with(session) {
            when (unwrapped) {
                is KtConstantExpression, is KtThisExpression -> true

                is KtStringTemplateExpression ->
                    unwrapped.entries.all { it is KtLiteralStringTemplateEntry || it is KtEscapeStringTemplateEntry }

                is KtSimpleNameExpression -> unwrapped.resolveToCall()?.successfulVariableAccessCall()?.symbol.let {
                    it is KaLocalVariableSymbol || it is KaValueParameterSymbol
                }

                else -> false
            }
        }
    }

    private fun KtSimpleNameExpression.usedSymbols(): List<KaSymbol> =
        with(session) {
            val applied = resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()?.partiallyAppliedSymbol
            applied?.run { contextArguments.mapNotNull { it.implicitSymbol() } + symbol }.orEmpty()
        }

    private fun KaReceiverValue.implicitSymbol(): KaSymbol? =
        when (this) {
            is KaSmartCastedReceiverValue -> original.implicitSymbol()
            is KaImplicitReceiverValue -> symbol
            is KaExplicitReceiverValue -> null
        }
}
