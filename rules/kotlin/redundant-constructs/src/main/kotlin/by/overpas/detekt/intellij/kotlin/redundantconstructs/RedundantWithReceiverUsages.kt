package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExperimentalApi
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.resolution.KtResolvableCall

@OptIn(KaExperimentalApi::class, KtExperimentalApi::class)
context(session: KaSession)
internal fun KtElement.usesReceiverOf(owner: KaSymbol): Boolean =
    with(session) {
        when (val element = this@usesReceiverOf) {
            is KtThisExpression ->
                (element.resolveSymbol() as? KaReceiverParameterSymbol)?.owningCallableSymbol == owner

            is KtResolvableCall ->
                element.resolveCall().singleCalls().flatMap { it.receiverValues() }.any { it.ownerSymbol() == owner }

            else -> false
        }
    }

context(session: KaSession)
internal fun KaReceiverValue.ownerSymbol(): KaSymbol? =
    when (this) {
        is KaImplicitReceiverValue -> with(session) { symbol.containingSymbol }
        is KaSmartCastedReceiverValue -> original.ownerSymbol()
        is KaExplicitReceiverValue -> null
    }
