package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaBackingFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal fun KtProperty.isSuspiciousVarCandidate(): Boolean =
    !isLocal &&
        isVar &&
        initializer != null &&
        setter == null &&
        getter?.bodyExpression != null &&
        !hasDelegate()

context(session: KaSession)
internal fun KtProperty.isSuspiciousVarProperty(): Boolean =
    !overridesVar() &&
        getter?.bodyExpression
            ?.collectDescendantsOfType<KtNameReferenceExpression> { reference ->
                reference.getReferencedName() == KtTokens.FIELD_KEYWORD.value
            }
            .orEmpty()
            .none { it.isBackingFieldOf(this) }

context(session: KaSession)
private fun KtProperty.overridesVar(): Boolean =
    hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
        with(session) {
            symbol.allOverriddenSymbols.filterIsInstance<KaPropertySymbol>().any { !it.isVal }
        }

context(session: KaSession)
private fun KtNameReferenceExpression.isBackingFieldOf(property: KtProperty): Boolean =
    with(session) {
        references.filterIsInstance<KtReference>().any { reference ->
            val symbol = reference.resolveToSymbol() as? KaBackingFieldSymbol
            symbol?.run { owningProperty.psi } == property
        }
    }
