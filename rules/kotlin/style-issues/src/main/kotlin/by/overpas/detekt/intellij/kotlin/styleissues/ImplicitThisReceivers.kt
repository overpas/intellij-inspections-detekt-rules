package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaImplicitReceiver
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

context(session: KaSession)
internal fun KtSimpleNameExpression.isImplicitThisAccess(): Boolean =
    with(session) {
        val reference = this@isImplicitThisAccess
        val symbol = reference.references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() }
        val receivers = reference.containingKtFile.scopeContext(reference).implicitReceivers
        symbol?.implicitThisClass()?.canBeReferencedByThisIn(receivers) == true
    }

context(session: KaSession)
private fun KaClassSymbol.canBeReferencedByThisIn(implicitReceivers: List<KaImplicitReceiver>): Boolean {
    val receivers = implicitReceivers.map { it.toImplicitThisReceiver() }
    val index = receivers.indexOfFirst { it == null || isImplicitThisSuperOf(it) }
    val match = receivers.getOrNull(index) ?: return false
    val outerLabels = receivers.take(index).mapNotNull { it?.label }
    return match.label !in outerLabels && (index == 0 || match.label != null)
}
