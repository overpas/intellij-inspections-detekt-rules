package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceAddAllWithMapTo(config: Config) :
    Rule(
        config,
        "Adding the result of `map` or `filter` to a mutable collection creates an intermediate list. " +
            "Use `mapTo` or `filterTo` with the collection as the destination instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.operationToken != KtTokens.PLUSEQ) return
        val operation = analyze(expression) { expression.replaceAddAllWithMapToOp() } ?: return
        report(
            Finding(
                Entity.from(expression.operationReference),
                "'+= $operation {}' should be replaced with '${operation}To'",
            ),
        )
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.replaceAddAllWithMapToCallee() ?: return
        val operation = analyze(expression) { expression.replaceAddAllWithMapToOp() } ?: return
        report(
            Finding(
                Entity.from(callee),
                "'${callee.getReferencedName()}($operation {})' should be replaced with '${operation}To'",
            ),
        )
    }
}
