package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class RedundantLabeledReturnOnLastExpressionInLambda(config: Config) :
    Rule(
        config,
        "A labeled return on the last expression of a lambda is redundant, because the last expression " +
            "is the result of the lambda. Remove the return.",
    ) {

    override fun visitReturnExpression(expression: KtReturnExpression) {
        super.visitReturnExpression(expression)
        val label = expression.getLabelName()
        if (label != null && label == expression.enclosingLambdaLabel()) {
            report(Finding(Entity.from(expression), "Remove the redundant `return@$label`."))
        }
    }

    private fun KtReturnExpression.enclosingLambdaLabel(): String? {
        val block = (parent as? KtBlockExpression)?.takeIf { it.statements.lastOrNull() == this }
        val lambda = (block?.parent as? KtFunctionLiteral)?.parent as? KtLambdaExpression
        val labeled = lambda?.parent as? KtLabeledExpression
        val argument = (labeled ?: lambda)?.parent as? KtValueArgument
        val call = argument?.getStrictParentOfType<KtCallExpression>()
        return call?.let { labeled?.getLabelName() ?: (it.calleeExpression as? KtSimpleNameExpression)?.text }
    }
}
