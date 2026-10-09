package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaImplicitReceiver
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.name.Name

internal data class ImplicitThisReceiver(
    val classSymbol: KaClassSymbol,
    val label: Name?,
)

context(session: KaSession)
internal fun KaImplicitReceiver.toImplicitThisReceiver(): ImplicitThisReceiver? =
    with(session) {
        val classSymbol = type.expandedSymbol
        val label = ownerSymbol.implicitThisLabel()
        if (classSymbol == null || label == null) null else ImplicitThisReceiver(classSymbol, label.name)
    }

context(session: KaSession)
internal fun KaClassSymbol.isImplicitThisSuperOf(receiver: ImplicitThisReceiver): Boolean =
    with(session) {
        val superClass = this@isImplicitThisSuperOf
        receiver.classSymbol == superClass || receiver.classSymbol.isSubClassOf(superClass)
    }
