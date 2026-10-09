package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor

class UseExpressionBody(config: Config) :
    Rule(
        config,
        "A block body holds only a single `return` or a single expression of type `Unit` or `Nothing`. " +
            "Use an expression body.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        function.reportIfConvertible()
    }

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        accessor.reportIfConvertible()
    }

    private fun KtDeclarationWithBody.reportIfConvertible() {
        val body = UseExpressionBodyBlock(bodyBlockExpression ?: return)
        val subject = body.takeIf { it.isConvertible() }?.let { analyze(this) { it.subject() } } ?: return
        report(Finding(Entity.from(body.highlighted()), "Use expression body instead of $subject"))
    }
}
