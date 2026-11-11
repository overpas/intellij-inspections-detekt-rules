package by.overpas.detekt.intellij.kotlin.logging.kotlinloggerinitializedwithforeignclass

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("KotlinLoggerInitializedWithForeignClass")
class KotlinLoggerInitializedWithForeignClass(config: Config) :
    Rule(
        config,
        "A logger is created with the class literal of another class, so its log records show the wrong source. " +
            "Pass the class literal of the class that holds the logger.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "The methods that create a logger, given as fully qualified names, " +
            "e.g. `org.slf4j.LoggerFactory.getLogger`.",
    )
    private val loggerFactories: List<String> by config(KOTLIN_LOGGER_INITIALIZED_WITH_FOREIGN_CLASS_FACTORIES)

    private val factories by lazy { KotlinLoggerInitializedWithForeignClassFactories(loggerFactories.toSet()) }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val classLiteral = expression.foreignLoggerClassLiteral(factories.methodNames) ?: return
        if (analyze(expression) { factories.isFactoryCall(expression) }) {
            report(
                Finding(
                    Entity.from(classLiteral),
                    "Logger initialized with foreign class '${classLiteral.receiverExpression?.text.orEmpty()}::class'",
                ),
            )
        }
    }
}
