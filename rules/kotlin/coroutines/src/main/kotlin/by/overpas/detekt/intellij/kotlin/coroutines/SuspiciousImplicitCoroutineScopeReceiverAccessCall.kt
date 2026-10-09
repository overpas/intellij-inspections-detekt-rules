package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.findLabelAndCall

@OptIn(KaExperimentalApi::class)
internal class SuspiciousImplicitCoroutineScopeReceiverAccessCall(
    private val session: KaSession,
    private val expression: KtExpression,
) {

    private val call: KaSingleCall<*, *>? = with(session) {
        val info = expression.resolveToCall()
        if (expression is KtCallExpression) {
            info?.successfulFunctionCallOrNull()
        } else {
            info?.successfulVariableAccessCall()
        }
    }

    private val receiver = (call?.extensionReceiver ?: call?.dispatchReceiver) as? KaImplicitReceiverValue

    private val owner = receiver?.symbol.let { symbol ->
        (symbol as? KaReceiverParameterSymbol)?.owningCallableSymbol ?: symbol as? KaClassLikeSymbol
    }

    private val hasLabel = when (val declaration = owner?.psi) {
        is KtFunctionLiteral -> declaration.findLabelAndCall().first != null
        is KtNamedDeclaration -> declaration.nameAsName != null
        else -> false
    }

    fun isSuspicious(): Boolean =
        with(session) {
            val scopeType = findClass(COROUTINE_SCOPE)?.defaultType
            val symbol = call?.run { signature.symbol }
            val declaredReceiverType = symbol?.receiverType
                ?: (symbol?.run { fakeOverrideOriginal.containingSymbol } as? KaClassLikeSymbol)?.defaultType
            val accessOwner = owner
            scopeType != null &&
                accessOwner != null &&
                hasLabel &&
                receiver?.run { type.semanticallyEquals(scopeType) } == true &&
                declaredReceiverType?.isSubtypeOf(scopeType) == true &&
                SuspiciousImplicitCoroutineScopeReceiverAccessPath(session, accessOwner).isSuspendingFrom(expression)
        }

    private companion object {
        val COROUTINE_SCOPE = ClassId(FqName("kotlinx.coroutines"), Name.identifier("CoroutineScope"))
    }
}
