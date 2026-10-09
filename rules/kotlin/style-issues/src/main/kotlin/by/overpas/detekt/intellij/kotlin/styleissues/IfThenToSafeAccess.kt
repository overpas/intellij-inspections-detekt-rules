package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtThisExpression

class IfThenToSafeAccess(config: Config) :
    Rule(
        config,
        "An `if` checks a value for `null` or for a type and returns `null` otherwise. " +
            "Replace it with a safe call `?.` or a safe cast `as?`.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val data = expression.ifThenToSafeAccessData() ?: return
        val condition = data.condition
        val isApplicable = data.negatedClause?.isIfThenNullLiteral() != false &&
            (condition !is KtIsExpression || condition.typeReference != null)
        val isFoldable = isApplicable && analyze(expression) {
            val usages = data.checkedUsages()
            val isUsedWithoutElse = data.negatedClause == null && data.baseClause.isUsedAsExpression
            (data.checkedExpression is KtThisExpression || usages.isNotEmpty()) &&
                !isUsedWithoutElse &&
                usages.all { it.ifThenSmartCastStability() == true } &&
                !condition.isSenselessIfThenCondition() &&
                IfThenToSafeAccessStrategy(this, data).isSuggested() == true
        }
        if (isFoldable) report(Finding(Entity.from(expression.ifKeyword), "Foldable if-then"))
    }
}
