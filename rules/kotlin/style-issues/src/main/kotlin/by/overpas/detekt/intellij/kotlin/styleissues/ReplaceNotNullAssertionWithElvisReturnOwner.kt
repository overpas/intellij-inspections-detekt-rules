package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.unpackFunctionLiteral

context(session: KaSession)
internal fun KtExpression.allowsElvisReturn(): Boolean =
    with(session) {
        when (val owner = this@allowsElvisReturn) {
            is KtNamedFunction -> owner.symbol.returnType.let { it.isNullable || it.isUnitType }
            is KtLambdaExpression -> owner.functionLiteral.symbol.returnType.isUnitType
            else -> false
        }
    }

internal fun KtExpression.hasElvisReturnLabel(): Boolean {
    val lambda = this as? KtLambdaExpression ?: return true
    val call = lambda.getStrictParentOfType<KtCallExpression>()
    val argument = call?.run {
        valueArguments.find { it.getArgumentExpression()?.unpackFunctionLiteral(allowParentheses = false) === lambda }
    }
    val label = (argument?.getArgumentExpression() as? KtLabeledExpression)?.getLabelName()
    return argument != null && (label ?: call.getCallNameExpression()?.text) != null
}
