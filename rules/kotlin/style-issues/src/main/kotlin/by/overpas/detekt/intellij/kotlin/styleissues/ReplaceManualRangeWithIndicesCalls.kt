package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

@IntellijInspection("ReplaceManualRangeWithIndicesCalls")
class ReplaceManualRangeWithIndicesCalls(config: Config) :
    Rule(
        config,
        "A manual range from `0` to the size of a collection, an array or a string is verbose. " +
            "Use `indices`, or loop over the elements or `withIndex()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        ReplaceManualRangeWithIndicesCallsRange(expression).message()?.let {
            report(Finding(Entity.from(expression), it))
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        ReplaceManualRangeWithIndicesCallsRange(expression).message()?.let {
            report(Finding(Entity.from(expression), it))
        }
    }
}
