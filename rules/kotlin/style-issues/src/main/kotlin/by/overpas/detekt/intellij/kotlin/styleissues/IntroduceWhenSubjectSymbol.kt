package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.KtReferenceExpression

context(session: KaSession)
internal fun KtReferenceExpression.whenSubjectSymbol(): KaSymbol? =
    with(session) { references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } }
