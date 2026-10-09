package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class RedundantElvisReturnNull(config: Config) :
    Rule(
        config,
        "`?: return null` in a returned expression is redundant, because the result is null anyway. " +
            "Remove the elvis operator.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val innerReturn = expression.right as? KtReturnExpression
        val outerReturn = expression.getStrictParentOfType<KtReturnExpression>()
        val isElvisReturnNull = expression.operationToken == KtTokens.ELVIS &&
            innerReturn?.returnedExpression?.let { KtPsiUtil.isNullConstant(it) } == true &&
            outerReturn?.returnedExpression?.let { KtPsiUtil.safeDeparenthesize(it) } == expression
        if (isElvisReturnNull && expression.isRedundant(innerReturn, outerReturn)) {
            report(Finding(Entity.from(expression), "Redundant '?: return null'"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtBinaryExpression.isRedundant(
        innerReturn: KtReturnExpression,
        outerReturn: KtReturnExpression,
    ): Boolean {
        val leftExpression = left
        return leftExpression != null &&
            analyze(this) {
                val outerTarget = outerReturn.resolveSymbol()
                outerTarget != null &&
                    outerTarget == innerReturn.resolveSymbol() &&
                    leftExpression.expressionType?.isMarkedNullable == true
            }
    }
}
