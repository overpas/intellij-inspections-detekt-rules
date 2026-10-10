package by.overpas.detekt.intellij.kotlin.namingconventions

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtValueArgument

@IntellijInspection("InconsistentCommentForJavaParameter")
class InconsistentCommentForJavaParameter(config: Config) :
    Rule(
        config,
        "A `/* name = */` comment before an argument of a Java call names another parameter. " +
            "Use the name of the parameter that the argument is passed to.",
    ),
    RequiresAnalysisApi {

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val comment = argument.parameterNameComment() ?: return
        val expectation = analyze(argument) { argument.javaParameterExpectation() } ?: return
        if (!expectation.isMetBy(comment)) {
            report(Finding(Entity.from(comment), "Inconsistent parameter name for '${expectation.parameterName}'"))
        }
    }
}
