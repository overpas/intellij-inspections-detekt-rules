package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtIfExpression

class FoldInitializerAndIfToElvis(config: Config) :
    Rule(
        config,
        "An `if` that only checks the variable declared right before it for `null` or for a type and then exits " +
            "can be folded into the initializer. Use `?:` with the exit statement.",
    ),
    RequiresAnalysisApi {

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val candidate = FoldInitializerAndIfToElvisCandidate(expression)
        if (candidate.isCandidate() && analyze(expression) { candidate.isFoldable() }) {
            report(Finding(Entity.from(expression), "If-Null return/break/... foldable to '?:'"))
        }
    }
}
