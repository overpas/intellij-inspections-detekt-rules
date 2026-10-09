package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class ConvertTwoComparisonsToRangeCheckContains(
    private val value: KtExpression,
    private val rangeText: String,
) {

    private val function = value.getStrictParentOfType<KtNamedFunction>()
        ?.takeIf { it.hasModifier(KtTokens.OPERATOR_KEYWORD) && it.nameAsName == OperatorNameConventions.CONTAINS }

    val isRecursive: Boolean
        get() {
            val contains = function ?: return false
            val fragment = KtPsiFactory(contains.project).createExpressionCodeFragment(rangeText, value)
            val check = fragment.getContentElement() as? KtBinaryExpression
            return check == null ||
                analyze(fragment) {
                    val target = check.operationReference.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
                    target == null || target.psi == contains
                }
        }
}
