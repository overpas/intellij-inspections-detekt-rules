package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

internal fun KtSimpleNameExpression.recursiveAccessorMessage(): String? =
    when {
        parent is KtCallableReferenceExpression -> null
        isRecursiveAccessorAccess() -> "Recursive property accessor"
        isRecursiveSyntheticAccess() -> "Recursive synthetic property accessor"
        else -> null
    }
