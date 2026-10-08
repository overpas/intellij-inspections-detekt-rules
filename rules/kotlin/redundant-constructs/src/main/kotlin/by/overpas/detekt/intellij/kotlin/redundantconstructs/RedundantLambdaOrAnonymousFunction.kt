package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.util.OperatorNameConventions

class RedundantLambdaOrAnonymousFunction(config: Config) :
    Rule(
        config,
        "A lambda or an anonymous function that is called at once is redundant. Inline its body.",
    ) {

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        if (lambdaExpression.functionLiteral.isCalledAtOnce()) {
            report(Finding(Entity.from(lambdaExpression), "Redundant lambda creation"))
        }
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.isCalledAtOnce()) {
            report(Finding(Entity.from(function), "Redundant anonymous function creation"))
        }
    }

    private fun KtFunction.isCalledAtOnce(): Boolean {
        if (!hasBody()) return false
        val usage = parents
            .takeWhile { it is KtParenthesizedExpression || it is KtLambdaExpression }
            .lastOrNull()
            ?.parent
        return usage is KtCallExpression ||
            (
                usage is KtQualifiedExpression &&
                    (usage.selectorExpression as? KtCallExpression)?.calleeExpression?.text ==
                    OperatorNameConventions.INVOKE.asString()
                )
    }
}
