package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

context(session: KaSession)
internal fun KtExpression?.hasBooleanConstantsToSimplify(): Boolean =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        null -> false

        is KtBinaryExpression ->
            expression.hasSimplifiableOperand() ||
                (!expression.isSignedZeroComparison() && expression.isBooleanConstant())

        else -> expression.isBooleanConstant()
    }

context(session: KaSession)
private fun KtExpression.isBooleanConstant(): Boolean =
    with(session) { evaluate() is KaConstantValue.BooleanValue }
