package by.overpas.detekt.intellij.kotlin.logging

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

class KotlinLoggerInitializedWithForeignClass(config: Config) :
    Rule(
        config,
        "A logger is created with the class literal of another class, so its log records show the wrong source. " +
            "Pass the class literal of the class that holds the logger.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val classLiteral = expression.foreignLoggerClassLiteral() ?: return
        if (analyze(expression) { expression.isLoggerFactoryCall() }) {
            report(
                Finding(
                    Entity.from(classLiteral),
                    "Logger initialized with foreign class '${classLiteral.receiverExpression?.text.orEmpty()}::class'",
                ),
            )
        }
    }
}
