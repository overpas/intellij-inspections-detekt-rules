package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

context(session: KaSession)
internal fun KtBinaryExpression.alternativesWhenSubject(search: IntroduceWhenSubjectSearch): KtExpression? {
    val leftCandidate = search.candidateOf(left?.let { KtPsiUtil.safeDeparenthesize(it) })
    val rightCandidate = search.candidateOf(right?.let { KtPsiUtil.safeDeparenthesize(it) })
    val isMatching = leftCandidate.matchesWhenSubject(rightCandidate)
    return leftCandidate.takeIf { isMatching }
}

internal fun KtBinaryExpression.guardedWhenSubject(search: IntroduceWhenSubjectSearch): KtExpression? =
    search.candidateOf(deepestAndAndLeft())

private fun KtBinaryExpression.deepestAndAndLeft(): KtExpression? {
    val deparenthesized = left?.let { KtPsiUtil.safeDeparenthesize(it) }
    val nested = deparenthesized as? KtBinaryExpression
    return if (nested?.operationToken == KtTokens.ANDAND) nested.deepestAndAndLeft() else deparenthesized
}
