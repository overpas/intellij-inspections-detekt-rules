package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class UsePropertyAccessSyntax(config: Config) :
    Rule(
        config,
        "A Java getter or setter is called as a method although Kotlin exposes it as a synthetic property. " +
            "Use property access syntax.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val accessor = expression.usePropertyAccessSyntaxCall() ?: return
        val property = analyze(expression) { accessor.syntheticProperty() } ?: return
        if (property.isResolvedBy(accessor.replacementText(property.name))) {
            report(Finding(Entity.from(expression), accessor.message))
        }
    }
}
