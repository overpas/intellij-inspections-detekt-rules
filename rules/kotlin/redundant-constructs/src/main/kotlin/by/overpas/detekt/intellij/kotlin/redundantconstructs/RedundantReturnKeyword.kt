package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.fir.diagnostics.KaFirDiagnostic
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.getOutermostParenthesizerOrThis

@IntellijInspection("RedundantReturnKeyword")
class RedundantReturnKeyword(config: Config) :
    Rule(
        config,
        "A 'return' keyword inside a branch of an expression that is already returned is redundant. Remove it.",
    ),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitReturnExpression(expression: KtReturnExpression) {
        super.visitReturnExpression(expression)
        val returned = expression.returnedExpression
        val isLabeledLambda = returned is KtLabeledExpression && returned.baseExpression is KtLambdaExpression
        val topMostOwner = generateSequence<KtExpression>(expression) { it.owner() }.last()
        val isInReturnedBranch = topMostOwner.getOutermostParenthesizerOrThis().parent is KtReturnExpression
        if (expression.getLabelName() != null || isLabeledLambda || !isInReturnedBranch) return
        val isUnreachable = analyze(expression) {
            expression.directDiagnostics(KaDiagnosticCheckerFilter.EXTENDED_AND_COMMON_CHECKERS)
                .any { it is KaFirDiagnostic.UnreachableCode }
        }
        if (!isUnreachable) report(Finding(Entity.from(expression.returnKeyword), "Redundant 'return' keyword"))
    }

    private fun KtExpression.owner(): KtExpression? {
        val normalized = getOutermostParenthesizerOrThis()
        val block = (normalized.parent as? KtBlockExpression)?.takeIf { it.statements.lastOrNull() === normalized }
        val subject = block ?: normalized
        return when (val container = subject.parent) {
            is KtContainerNodeForControlStructureBody ->
                (container.takeIf { it.expression === subject }?.parent as? KtIfExpression)?.let { innermost ->
                    generateSequence(innermost) { ifExpression ->
                        (ifExpression.parent as? KtContainerNodeForControlStructureBody)?.parent as? KtIfExpression
                    }.last()
                }

            is KtWhenEntry -> container.takeIf { it.expression === subject }?.parent as? KtWhenExpression

            is KtBinaryExpression -> container.takeIf { elvis ->
                elvis.operationToken == KtTokens.ELVIS &&
                    elvis.right === normalized &&
                    (normalized as? KtReturnExpression)?.returnedExpression?.let(KtPsiUtil::isNullConstant) != true
            }

            else -> null
        }
    }
}
