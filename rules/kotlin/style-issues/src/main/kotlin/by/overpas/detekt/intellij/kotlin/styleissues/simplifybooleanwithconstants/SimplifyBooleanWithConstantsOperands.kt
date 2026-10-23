package by.overpas.detekt.intellij.kotlin.styleissues.simplifybooleanwithconstants

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
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
    PsiTreeUtil.findChildOfType(this, KtNameReferenceExpression::class.java, false) == null &&
        with(session) { evaluate() is KaConstantValue.BooleanValue }
