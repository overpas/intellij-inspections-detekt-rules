package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

internal class ReplaceToStringWithStringTemplateCall(private val expression: KtDotQualifiedExpression) {

    fun isConversion(): Boolean {
        val call = expression.selectorExpression as? KtCallExpression ?: return false
        val callee = call.calleeExpression as? KtNameReferenceExpression
        val isCandidate = expression.receiverExpression is KtReferenceExpression &&
            expression.parent !is KtBlockStringTemplateEntry &&
            call.valueArguments.isEmpty() &&
            callee?.getReferencedName() == "toString"
        return isCandidate && analyze(call) {
            val function = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            function != null && function.valueParameters.isEmpty() && function.returnType.isStringType
        }
    }
}
