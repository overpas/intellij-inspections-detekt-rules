package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtElement

context(session: KaSession)
internal fun KtElement.liftReferencedSymbol(): KaSymbol? =
    with(session) { references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } }
