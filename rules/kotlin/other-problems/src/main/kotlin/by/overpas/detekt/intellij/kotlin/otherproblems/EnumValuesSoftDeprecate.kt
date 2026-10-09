package by.overpas.detekt.intellij.kotlin.otherproblems

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("EnumValuesSoftDeprecate")
class EnumValuesSoftDeprecate(config: Config) :
    Rule(
        config,
        "`Enum.values()` creates a new array on each call and is soft-deprecated since Kotlin 1.9. " +
            "Use `Enum.entries` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.text != "values()") return
        if (analyze(expression) { expression.callsSoftDeprecatedEnumValues() }) {
            report(
                Finding(
                    Entity.from(expression),
                    "'Enum.values()' is recommended to be replaced by 'Enum.entries' since 1.9",
                ),
            )
        }
    }
}
