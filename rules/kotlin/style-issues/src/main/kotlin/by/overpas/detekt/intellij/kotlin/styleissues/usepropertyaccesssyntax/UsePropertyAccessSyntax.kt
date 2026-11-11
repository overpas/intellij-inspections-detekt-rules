package by.overpas.detekt.intellij.kotlin.styleissues.usepropertyaccesssyntax

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.kotlin.styleissues.message
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("UsePropertyAccessSyntax")
class UsePropertyAccessSyntax(config: Config) :
    Rule(
        config,
        "A Java getter or setter is called as a method although Kotlin exposes it as a synthetic property. " +
            "Use property access syntax.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Do not report the calls of these Java methods, given as fully qualified names, " +
            "e.g. `java.net.Socket.getInputStream`.",
    )
    private val ignoredFunctions: List<String> by config(USE_PROPERTY_ACCESS_SYNTAX_IGNORED_FUNCTIONS)

    @Configuration("Also report the accessors whose body has more than one statement.")
    private val areNonTrivialAccessorsReported: Boolean by config(false)

    private val options by lazy {
        UsePropertyAccessSyntaxOptions(ignoredFunctions.toSet(), areNonTrivialAccessorsReported)
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val accessor = expression.usePropertyAccessSyntaxCall() ?: return
        val property = analyze(expression) { accessor.syntheticProperty(options) } ?: return
        if (property.isResolvedBy(accessor.replacementText(property.name))) {
            report(Finding(Entity.from(expression), accessor.message))
        }
    }
}
