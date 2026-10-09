package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIsExpression

internal class IntroduceWhenSubjectSearch(
    private val session: KaSession,
    private val state: IntroduceWhenSubjectState,
) {

    fun candidateOf(expression: KtExpression?): KtExpression? {
        val binary = expression as? KtBinaryExpression
        val token = binary?.operationToken
        return context(session) {
            when {
                state == IntroduceWhenSubjectState.INCOMPATIBLE -> null

                expression is KtIsExpression -> expression.leftHandSide

                token == KtTokens.IN_KEYWORD || token == KtTokens.NOT_IN -> binary.left

                token == KtTokens.EQEQ -> binary.whenSubjectEqualityOperand()

                token == KtTokens.OROR ->
                    binary.alternativesWhenSubject(
                        IntroduceWhenSubjectSearch(session, state + IntroduceWhenSubjectState.OR_ONLY),
                    )

                token == KtTokens.ANDAND && state != IntroduceWhenSubjectState.GUARDS_NOT_SUPPORTED ->
                    binary.guardedWhenSubject(
                        IntroduceWhenSubjectSearch(session, state + IntroduceWhenSubjectState.AND_ONLY),
                    )

                else -> null
            }
        }
    }
}
