package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.OppositeRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtExpression

@OppositeRule("ExplicitThis")
@IntellijInspection("ImplicitThis")
class ImplicitThis(config: Config) :
    Rule(
        config,
        "A member is accessed through an implicit `this` receiver. Add an explicit `this` to show the receiver.",
    ),
    RequiresAnalysisApi {

    override fun visitExpression(expression: KtExpression) {
        super.visitExpression(expression)
        val reference = expression.implicitThisReference() ?: return
        if (analyze(reference) { reference.isImplicitThisAccess() }) {
            report(Finding(Entity.from(expression), "Implicit 'this'"))
        }
    }
}
