package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression

internal data class IfThenToSafeAccessClauses(
    val base: KtExpression,
    private val ifExpression: KtIfExpression,
) {

    val negated: KtExpression? = listOfNotNull(ifExpression.then, ifExpression.`else`)
        .mapNotNull { it.ifThenSingleStatement() }
        .firstOrNull { it != base }
}
