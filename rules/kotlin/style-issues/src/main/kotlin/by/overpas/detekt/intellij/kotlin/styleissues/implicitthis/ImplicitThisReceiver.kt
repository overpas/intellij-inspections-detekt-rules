package by.overpas.detekt.intellij.kotlin.styleissues.implicitthis

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.KaImplicitReceiver
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtElement

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

context(session: KaSession)
internal fun KaImplicitReceiver.isDeclaredAround(element: KtElement): Boolean =
    with(session) {
        val companion = type.expandedSymbol?.takeIf { it.classKind == KaClassKind.COMPANION_OBJECT }
        companion == null || PsiTreeUtil.isAncestor(companion.psi, element, false)
    }
