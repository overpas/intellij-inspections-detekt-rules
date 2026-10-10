package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("DestructuringDeclaration")
class DestructuringDeclaration(config: Config) :
    Rule(
        config,
        "A local variable, loop variable or lambda parameter of a data class or `Map.Entry` type is used only " +
            "to read its components. Use a destructuring declaration instead.",
    ),
    RequiresAnalysisApi {

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        parameter.reportIfDestructurable()
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        property.reportIfDestructurable()
    }

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        lambdaExpression.functionLiteral.reportIfDestructurable()
    }

    private fun KtDeclaration.reportIfDestructurable() {
        val candidate = DestructuringDeclarationCandidate(this)
        val highlight = candidate.highlight
        if (highlight != null && analyze(this) { candidate.isDestructurable() }) {
            report(Finding(Entity.from(highlight), "Use destructuring declaration"))
        }
    }
}
