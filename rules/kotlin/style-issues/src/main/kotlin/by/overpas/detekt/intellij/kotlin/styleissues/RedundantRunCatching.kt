package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtQualifiedExpression

class RedundantRunCatching(config: Config) :
    Rule(
        config,
        "`runCatching { }.getOrThrow()` rethrows every exception it catches. Replace it with `run { }`.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val runCatchingCall = expression.redundantRunCatchingCandidate() ?: return
        val callee = runCatchingCall.calleeExpression ?: return
        if (analyze(expression) { expression.isRedundantRunCatchingChain(runCatchingCall) }) {
            report(Finding(Entity.from(callee), "Redundant 'runCatching' call may be reduced to 'run'"))
        }
    }
}
