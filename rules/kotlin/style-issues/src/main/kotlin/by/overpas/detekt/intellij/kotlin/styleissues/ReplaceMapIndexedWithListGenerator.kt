package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

class ReplaceMapIndexedWithListGenerator(config: Config) :
    Rule(
        config,
        "`mapIndexed` on a collection whose lambda ignores the element only needs the index. " +
            "Replace it with the `List(size) { index -> ... }` generator.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val function = expression.mapIndexedGeneratorCandidate() ?: return
        val isReplaceable = analyze(expression) {
            val call = expression.resolveToCall()?.successfulFunctionCallOrNull()
            val symbol = call?.symbol as? KaNamedFunctionSymbol
            val receiver = call?.dispatchReceiver ?: call?.extensionReceiver
            symbol?.callableId?.asSingleFqName() == REPLACE_MAP_INDEXED_FQ_NAME &&
                receiver?.run { type.isSubtypeOf(StandardClassIds.Collection) } == true &&
                function.valueParameters[1].mapIndexedDeclarations().none { function.mapIndexedReferences(it) }
        }
        if (isReplaceable) {
            report(
                Finding(
                    Entity.from(expression.calleeExpression ?: expression),
                    "Should be replaced with List generator",
                ),
            )
        }
    }
}
