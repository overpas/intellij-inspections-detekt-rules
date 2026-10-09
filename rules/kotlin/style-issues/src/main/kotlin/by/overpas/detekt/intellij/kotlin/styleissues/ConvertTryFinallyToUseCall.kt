package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtTryExpression

class ConvertTryFinallyToUseCall(config: Config) :
    Rule(
        config,
        "A `try`/`finally` block that only closes a `Closeable` resource in `finally` duplicates `use()`. " +
            "Call `use()` on the resource instead.",
    ),
    RequiresAnalysisApi {

    override fun visitTryExpression(expression: KtTryExpression) {
        super.visitTryExpression(expression)
        if (expression.catchClauses.isEmpty() && ConvertTryFinallyToUseCallClose(expression).isCloseableClose) {
            report(Finding(Entity.from(expression), "try-finally can be replaced with 'use()'"))
        }
    }
}
