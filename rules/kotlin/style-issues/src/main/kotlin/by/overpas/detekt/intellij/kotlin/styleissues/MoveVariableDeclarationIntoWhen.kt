package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("MoveVariableDeclarationIntoWhen")
class MoveVariableDeclarationIntoWhen(config: Config) :
    Rule(
        config,
        "A variable that is declared right before a `when` and used only in it can be the `when` subject. " +
            "Move the declaration into the `when` subject.",
    ),
    RequiresAnalysisApi {

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        val subject = expression.subjectExpression ?: return
        val property = expression.moveIntoWhenProperty()
            ?.takeIf { it.nameIdentifier?.text == subject.text && it.isMovableIntoWhen(expression) }
            ?: return
        val message = "Variable declaration could be moved into 'when'"
        report(Finding(Entity.from(property.nameIdentifier ?: property), message))
    }
}
