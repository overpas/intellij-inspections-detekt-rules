package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaExplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector

context(session: KaSession)
internal fun KtSimpleNameExpression.recursiveAccessorTargets(accessor: KtPropertyAccessor): Boolean =
    with(session) {
        val call = resolveToCall()?.successfulVariableAccessCall()
        val symbol = call?.symbol as? KaPropertySymbol
        symbol != null &&
            symbol == accessor.property.symbol &&
            symbol.containingSymbol as? KaClassLikeSymbol == call.dispatchReceiver?.ownerSymbol() &&
            symbol.receiverParameter == call.extensionReceiver?.ownerSymbol()
    }

context(session: KaSession)
internal fun KtSimpleNameExpression.recursiveSyntheticTargets(function: KtNamedFunction): Boolean =
    with(session) {
        val symbol = references.filterIsInstance<KtReference>()
            .firstNotNullOfOrNull { it.resolveToSymbol() } as? KaSyntheticJavaPropertySymbol
        val functionSymbol = function.symbol
        symbol != null && (functionSymbol == symbol.javaGetterSymbol || functionSymbol == symbol.javaSetterSymbol)
    }

context(session: KaSession)
private fun KaReceiverValue.ownerSymbol(): KaSymbol? =
    when (val value = (this as? KaSmartCastedReceiverValue)?.original ?: this) {
        is KaImplicitReceiverValue -> value.symbol
        is KaExplicitReceiverValue -> value.expression.qualifierSymbol()
        else -> null
    }

context(session: KaSession)
private fun KtExpression.qualifierSymbol(): KaSymbol? =
    with(session) {
        val selector = getQualifiedElementSelector() ?: this@qualifierSymbol
        val reference = (selector as? KtThisExpression)?.instanceReference ?: selector
        val symbol = reference.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        if (symbol is KaClassSymbol) expressionType?.expandedSymbol else symbol
    }
