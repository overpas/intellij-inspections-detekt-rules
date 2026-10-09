package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class ReplaceRangeStartEndInclusiveWithFirstLast(config: Config) :
    Rule(
        config,
        "`start` and `endInclusive` of a primitive range return boxed values. Use `first` and `last` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val replacement = expression.unboxedRangeBoundReplacement() ?: return
        report(
            Finding(
                Entity.from(expression.selectorExpression ?: expression),
                "Could be replaced with unboxed '$replacement'",
            ),
        )
    }
}
