package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceReadLineWithReadln(config: Config) :
    Rule(
        config,
        "`readLine()` is less explicit about the end of input. " +
            "Use `readln()` instead of `readLine()!!` and `readlnOrNull()` instead of `readLine()`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.isReadLineCall()) {
            report(
                Finding(
                    Entity.from(expression.readLineReplacementTarget()),
                    "'readLine' can be replaced with 'readln' or 'readlnOrNull'",
                ),
            )
        }
    }
}
