package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

context(session: KaSession)
internal fun KtExpression?.matchesWhenSubject(other: KtExpression?): Boolean =
    when {
        this == null || other == null -> this == null && other == null
        text.filterNot { it.isWhitespace() } != other.text.filterNot { it.isWhitespace() } -> false
        else -> referencedSymbols() == other.referencedSymbols()
    }

context(session: KaSession)
private fun KtExpression.referencedSymbols(): List<KaSymbol?> =
    collectDescendantsOfType<KtReferenceExpression>().map { it.whenSubjectSymbol() }
