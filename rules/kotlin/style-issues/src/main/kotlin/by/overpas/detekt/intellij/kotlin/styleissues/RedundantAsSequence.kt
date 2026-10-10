package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

@IntellijInspection("RedundantAsSequence")
class RedundantAsSequence(config: Config) :
    Rule(
        config,
        "An `asSequence()` call on a sequence, or on an iterable that is followed by a single terminal operation, " +
            "is redundant. Remove the call.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val callee = (expression.selectorExpression as? KtCallExpression)?.calleeExpression ?: return
        if (callee.text != "asSequence") return
        val isRedundant = analyze(expression) {
            when (expression.asSequenceReceiverClassId()) {
                StandardClassIds.Sequence -> expression.isRedundantOnSequence()
                StandardClassIds.Iterable -> expression.isRedundantOnIterable()
                else -> false
            }
        }
        if (isRedundant) report(Finding(Entity.from(callee), "Redundant 'asSequence' call"))
    }
}
