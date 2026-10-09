package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaMultiCall
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
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
internal fun KtElement.usesNestedEachReceiverOf(owner: KaSymbol): Boolean =
    with(session) {
        when (val element = this@usesNestedEachReceiverOf) {
            is KtThisExpression -> {
                (element.resolveSymbol() as? KaReceiverParameterSymbol)?.owningCallableSymbol == owner
            }

            is KtResolvableCall -> {
                val calls = when (val call = element.resolveCall()) {
                    is KaSingleCall<*, *> -> listOf(call)
                    is KaMultiCall -> call.calls
                    null -> emptyList()
                }
                calls.flatMap { listOfNotNull(it.dispatchReceiver, it.extensionReceiver) }
                    .any { it.nestedEachOwner() == owner }
            }

            else -> {
                false
            }
        }
    }

context(session: KaSession)
private fun KaReceiverValue.nestedEachOwner(): KaSymbol? =
    with(session) {
        when (val receiver = this@nestedEachOwner) {
            is KaImplicitReceiverValue -> receiver.symbol.containingSymbol

            is KaSmartCastedReceiverValue -> (receiver.original as? KaImplicitReceiverValue)?.run {
                symbol.containingSymbol
            }

            is KaExplicitReceiverValue -> null
        }
    }
