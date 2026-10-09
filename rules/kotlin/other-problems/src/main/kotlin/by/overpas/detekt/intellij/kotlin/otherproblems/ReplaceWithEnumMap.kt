package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceWithEnumMap(config: Config) :
    Rule(
        config,
        "A `HashMap` with enum keys is less efficient than an `EnumMap` on the JVM. " +
            "Create a `java.util.EnumMap` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.valueArguments.isNotEmpty() || !expression.mayCreateHashMap()) return
        if (analyze(expression) { expression.createsHashMapWithEnumKeys() }) {
            report(Finding(Entity.from(expression.calleeExpression ?: expression), "Can be replaced with 'EnumMap'"))
        }
    }
}
