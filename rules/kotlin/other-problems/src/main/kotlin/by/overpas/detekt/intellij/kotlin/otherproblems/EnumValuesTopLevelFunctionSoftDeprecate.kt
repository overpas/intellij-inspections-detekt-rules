package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class EnumValuesTopLevelFunctionSoftDeprecate(config: Config) :
    Rule(
        config,
        "`enumValues<T>()` creates a new array on each call and is soft-deprecated. " +
            "Use `enumEntries<T>()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "enumValues") return
        if (analyze(expression) { expression.callsEnumValuesOfEnumClass() }) {
            report(
                Finding(
                    Entity.from(expression),
                    "'enumValues' is recommended to be replaced by 'enumEntries'",
                ),
            )
        }
    }
}
