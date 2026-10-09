package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtQualifiedExpression

class SimplifiableFlowCallChain(config: Config) :
    Rule(
        config,
        "A chain of two `Flow` calls can be one call, such as `filter {}.first()` as `first {}`. " +
            "Merge the calls.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val chain = SimplifiableFlowCallChainCalls(expression)
        val callee = chain.firstCallee ?: return
        if (chain.isCandidate() && analyze(expression) { chain.isSimplifiable(this) }) {
            report(Finding(Entity.from(callee), "Call chain on a 'Flow' type may be simplified"))
        }
    }
}
