package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtQualifiedExpression

@IntellijInspection("SimplifiableCallChain")
class SimplifiableCallChain(config: Config) :
    Rule(
        config,
        "Two chained collection, sequence or text calls such as `filter { }.first()` or `map { }.joinToString()` " +
            "can be merged into one call. Use the single call that does both.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val chain = SimplifiableCallChainCandidate(expression)
        val callee = chain.firstCallee
        if (callee != null && chain.isCandidate() && analyze(expression) { chain.isSimplifiable() }) {
            report(Finding(Entity.from(callee), "Call chain on a collection type may be simplified"))
        }
    }
}
