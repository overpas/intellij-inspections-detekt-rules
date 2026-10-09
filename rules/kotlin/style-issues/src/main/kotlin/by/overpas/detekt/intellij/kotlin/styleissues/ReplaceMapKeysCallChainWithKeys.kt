package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtQualifiedExpression

class ReplaceMapKeysCallChainWithKeys(config: Config) :
    Rule(
        config,
        "`map { it.key }.toSet()` on a map builds the set of keys the map already exposes. Use the `keys` property.",
    ),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val chain = expression.replaceMapKeysChain() ?: return
        if (analyze(expression) { chain.isReplaceable() }) {
            report(Finding(Entity.from(chain.mapCallee), "Call chain can be replaced with 'keys'"))
        }
    }
}
