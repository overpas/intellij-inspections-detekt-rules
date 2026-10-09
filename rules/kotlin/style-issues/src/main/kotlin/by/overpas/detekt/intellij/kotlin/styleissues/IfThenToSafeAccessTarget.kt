package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
internal fun KtExpression.ifThenTargetSymbol(): KaSymbol? =
    with(session) {
        val target = (this@ifThenTargetSymbol as? KtQualifiedExpression)?.selectorExpression ?: this@ifThenTargetSymbol
        val symbol = if (target is KtThisExpression) {
            target.resolveSymbol()
        } else {
            target.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        }
        symbol?.ifThenReceiverOwner()
    }

internal fun KaSymbol.ifThenReceiverOwner(): KaSymbol =
    (this as? KaReceiverParameterSymbol)?.owningCallableSymbol ?: this
