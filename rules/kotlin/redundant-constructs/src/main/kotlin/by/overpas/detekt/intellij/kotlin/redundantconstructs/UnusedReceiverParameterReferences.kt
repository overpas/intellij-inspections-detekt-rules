package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtDestructuringDeclarationEntry
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExperimentalApi
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolution.KtResolvable
import org.jetbrains.kotlin.resolution.KtResolvableCall

@OptIn(KaExperimentalApi::class, KtExperimentalApi::class)
internal class UnusedReceiverParameterReferences(
    private val session: KaSession,
    private val symbol: KaCallableSymbol,
    private val receiverType: KaType,
    private val reifiedParameters: Set<KaTypeParameterSymbol>,
) {

    fun usesThis(element: KtElement): Boolean =
        with(session) {
            element is KtThisExpression &&
                (element.resolveSymbol() as? KaReceiverParameterSymbol)?.owningCallableSymbol == symbol
        }

    fun usesClassLiteral(element: KtElement): Boolean =
        with(session) {
            val typeParameter = (element as? KtClassLiteralExpression)?.typeParameter()
            typeParameter != null && receiverType.semanticallyEquals(typeParameter.defaultType)
        }

    fun usesOperator(element: KtElement): Boolean {
        val resolvable: KtResolvable? = when (element) {
            is KtDestructuringDeclarationEntry -> element
            is KtForExpression -> element
            is KtProperty -> element.delegate
            else -> null
        }
        return resolvable?.hasReceiverOperator() == true
    }

    fun usesCall(element: KtElement): Boolean =
        with(session) {
            val call = (element as? KtResolvableCall)?.resolveCall() as? KaSingleCall<*, *>
            call != null &&
                (listOfNotNull(call.dispatchReceiver, call.extensionReceiver) + call.contextArguments)
                    .any { it.owner() == symbol }
        }

    fun usesReifiedType(element: KtElement): Boolean =
        with(session) {
            val typeParameter = when (element) {
                is KtTypeReference -> (element.type as? KaTypeParameterType)?.symbol
                is KtClassLiteralExpression -> element.typeParameter()
                else -> null
            }
            val function = element.getStrictParentOfType<KtFunction>()
            typeParameter in reifiedParameters &&
                function != null &&
                element.parents.any { it == function.bodyBlockExpression || it == function.bodyExpression }
        }

    private fun KtClassLiteralExpression.typeParameter(): KaTypeParameterSymbol? =
        with(session) {
            val argument = (expressionType as? KaClassType)?.run { typeArguments.firstOrNull()?.type }
            (argument as? KaTypeParameterType)?.symbol
        }

    private fun KtResolvable.hasReceiverOperator(): Boolean =
        with(session) {
            val receiverSymbol = receiverType.expandedSymbol
            resolveSymbols().filterIsInstance<KaFunctionSymbol>().any { it.containingDeclaration == receiverSymbol }
        }

    private fun KaReceiverValue.owner(): KaSymbol? {
        val value = this
        return with(session) {
            when (value) {
                is KaExplicitReceiverValue ->
                    (KtPsiUtil.deparenthesize(value.expression) as? KtThisExpression)?.resolveSymbol()?.containingSymbol

                is KaImplicitReceiverValue -> value.symbol.containingSymbol

                is KaSmartCastedReceiverValue -> value.original.owner()
            }
        }
    }
}
