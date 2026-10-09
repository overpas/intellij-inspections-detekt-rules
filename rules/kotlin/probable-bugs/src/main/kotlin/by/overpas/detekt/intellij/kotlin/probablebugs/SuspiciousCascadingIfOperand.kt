package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal fun KtIfExpression.suspiciousCascadingIfOperand(): KtExpression? =
    when (val branch = generateSequence(`else`) { (it as? KtIfExpression)?.`else` }.lastOrNull()) {
        is KtQualifiedExpression -> branch.receiverExpression
        is KtBinaryExpression -> branch.left
        else -> null
    }
