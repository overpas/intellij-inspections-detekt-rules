package by.overpas.detekt.intellij.kotlin.styleissues.collectionconcatenationtobuildcollection

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("CollectionConcatenationToBuildCollection")
class CollectionConcatenationToBuildCollection(config: Config) :
    Rule(
        config,
        "Each `+` or `-` on a list or set creates a new collection. " +
            "Use `buildList` or `buildSet` for a chain of such operations.",
    ),
    RequiresAnalysisApi {

    @Configuration("The minimal number of `+` or `-` operations in a chain to report it.")
    private val minimalOperationCount: Int by config(2)

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val chain = CollectionConcatenationToBuildCollectionChain(expression)
        if (chain.isCandidate(minimalOperationCount) && analyze(expression) { chain.isConvertible() }) {
            report(
                Finding(
                    Entity.from(chain.highlight),
                    "Collection concatenation can be converted to collection builder",
                ),
            )
        }
    }
}
