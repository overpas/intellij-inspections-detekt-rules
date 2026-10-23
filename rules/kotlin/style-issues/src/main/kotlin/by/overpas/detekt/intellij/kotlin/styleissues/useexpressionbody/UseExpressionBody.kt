package by.overpas.detekt.intellij.kotlin.styleissues.useexpressionbody

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression

@IntellijInspection("UseExpressionBody")
class UseExpressionBody(config: Config) :
    Rule(
        config,
        "A block body holds only a single one-line `return`. Use an expression body.",
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
        val body = bodyBlockExpression ?: return
        val statement = body.statements.singleOrNull() as? KtReturnExpression ?: return
        if (UseExpressionBodyReturn(statement).isOneLine()) {
            report(Finding(Entity.from(statement.returnKeyword), "Use expression body instead of one-line return"))
        }
    }
}
