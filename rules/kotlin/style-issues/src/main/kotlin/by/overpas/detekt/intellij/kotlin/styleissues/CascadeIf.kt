package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtBreakExpression
import org.jetbrains.kotlin.psi.KtContinueExpression
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

@IntellijInspection("CascadeIf")
class CascadeIf(config: Config) :
    Rule(
        config,
        "A chain of `if`-`else if` branches that check the same subject is hard to read. Replace it with `when`.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val ifs = generateSequence(expression) { it.`else` as? KtIfExpression }.toList()
        val branches = ifs.map { it.then } + listOfNotNull(ifs.last().`else`)
        val subjects = ifs.map { CascadeIfCondition(it.condition, null).subject?.cascadeIfSubjectText }
        val isCascade = branches.size > 2 &&
            '\n' in expression.text &&
            branches.none { it == null || it.lastBlockStatementOrThis() is KtIfExpression } &&
            expression.parent.node.elementType != KtNodeTypes.ELSE &&
            !expression.anyDescendantOfType<KtExpressionWithLabel> {
                it is KtBreakExpression || it is KtContinueExpression
            }
        if (isCascade && null !in subjects && subjects.distinct().size == 1) {
            report(Finding(Entity.from(expression.ifKeyword), "Cascade 'if' should be replaced with 'when'"))
        }
    }
}
