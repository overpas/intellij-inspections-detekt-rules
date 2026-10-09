package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.psi.KtExperimentalApi
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolution.KtResolvableCall

@OptIn(KaExperimentalApi::class, KtExperimentalApi::class)
context(session: KaSession)
internal fun KtExpression.ifThenImplicitReceiverOwners(): List<KaSymbol> =
    with(session) {
        val call = (this@ifThenImplicitReceiverOwners as? KtResolvableCall)?.resolveCall() as? KaSingleCall<*, *>
        listOfNotNull(call?.dispatchReceiver, call?.extensionReceiver)
            .mapNotNull { it.unwrapIfThenSmartCast() as? KaImplicitReceiverValue }
            .map { it.symbol.ifThenReceiverOwner() }
    }

private tailrec fun KaReceiverValue.unwrapIfThenSmartCast(): KaReceiverValue =
    if (this is KaSmartCastedReceiverValue) original.unwrapIfThenSmartCast() else this
