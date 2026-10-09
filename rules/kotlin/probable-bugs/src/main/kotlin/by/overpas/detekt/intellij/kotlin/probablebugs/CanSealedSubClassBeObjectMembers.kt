package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClass

internal fun KtProperty.hasSealedSubClassState(): Boolean =
    when {
        hasModifier(KtTokens.ABSTRACT_KEYWORD) || containingClass()?.isInterface() == true -> false
        initializer != null -> true
        delegate != null -> false
        !isVar -> getter == null
        else -> getter == null || setter == null
    }

internal fun KtNamedFunction.isSealedSubClassEquality(): Boolean =
    typeParameters.isEmpty() &&
        ((name == "equals" && valueParameters.size == 1) || (name == "hashCode" && valueParameters.isEmpty()))
