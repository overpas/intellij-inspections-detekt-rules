package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtThisExpression

context(session: KaSession)
internal fun KtExpression.selfAssignmentVariable(): KaVariableSymbol? =
    variableCall()?.symbol

context(session: KaSession)
internal fun KtExpression.selfAssignmentReceiver(): KaSymbol? {
    val receiver = (this as? KtDotQualifiedExpression)?.receiverExpression
    val reference = (receiver as? KtThisExpression)?.instanceReference ?: receiver
    return if (reference == null) {
        variableCall()?.run { dispatchReceiver ?: extensionReceiver }?.implicitSymbol()
    } else {
        with(session) {
            reference.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        }
    }
}

context(session: KaSession)
private fun KtExpression.variableCall(): KaVariableAccessCall? =
    with(session) { resolveToCall()?.successfulVariableAccessCall() }

private fun KaReceiverValue.implicitSymbol(): KaSymbol? =
    when (this) {
        is KaImplicitReceiverValue -> symbol
        is KaSmartCastedReceiverValue -> (original as? KaImplicitReceiverValue)?.symbol
        is KaExplicitReceiverValue -> null
    }
