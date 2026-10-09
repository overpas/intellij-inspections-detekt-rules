package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private val simplifyBooleanWithConstantsOperators = setOf(
    KtTokens.ANDAND,
    KtTokens.OROR,
    KtTokens.EQEQ,
    KtTokens.EXCLEQ,
)

private val simplifyBooleanWithConstantsEqualities = setOf(KtTokens.EQEQ, KtTokens.EQEQEQ)

context(session: KaSession)
internal fun KtBinaryExpression.hasSimplifiableOperand(): Boolean =
    operationToken in simplifyBooleanWithConstantsOperators &&
        (
            (left.hasBooleanConstantsToSimplify() && right.isNotNullBoolean()) ||
                (right.hasBooleanConstantsToSimplify() && left.isNotNullBoolean())
            )

context(session: KaSession)
internal fun KtBinaryExpression.isSignedZeroComparison(): Boolean {
    val values = listOfNotNull(left, right).map { KtPsiUtil.safeDeparenthesize(it).constantValue() }
    return operationToken in simplifyBooleanWithConstantsEqualities &&
        values.any { it == 0.0 || it == 0.0f } &&
        values.any { it == -0.0 || it == -0.0f }
}

context(session: KaSession)
private fun KtExpression.constantValue(): Any? =
    with(session) { evaluate()?.value }

context(session: KaSession)
private fun KtExpression?.isNotNullBoolean(): Boolean =
    with(session) {
        val type = this@isNotNullBoolean?.expressionType
        type != null && type.isBooleanType && !type.isMarkedNullable
    }
