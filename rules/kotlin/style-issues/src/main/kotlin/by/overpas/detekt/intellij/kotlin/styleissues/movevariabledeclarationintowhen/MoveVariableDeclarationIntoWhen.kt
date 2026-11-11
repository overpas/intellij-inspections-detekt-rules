package by.overpas.detekt.intellij.kotlin.styleissues.movevariabledeclarationintowhen

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.kotlin.styleissues.message
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtWhenExpression

@IntellijInspection("MoveVariableDeclarationIntoWhen")
class MoveVariableDeclarationIntoWhen(config: Config) :
    Rule(
        config,
        "A variable that is declared right before a `when` and used only in it can be the `when` subject. " +
            "Move the declaration into the `when` subject.",
    ),
    RequiresAnalysisApi {

    @Configuration("The maximum length of the line with the `when` subject after the move.")
    private val maxLineLength: Int by config(120)

    override fun visitWhenExpression(expression: KtWhenExpression) {
        super.visitWhenExpression(expression)
        val subject = expression.subjectExpression ?: return
        val property = expression.moveIntoWhenProperty()
            ?.takeIf { it.nameIdentifier?.text == subject.text && it.isMovableIntoWhen(expression, maxLineLength) }
            ?: return
        val message = "Variable declaration could be moved into 'when'"
        report(Finding(Entity.from(property.nameIdentifier ?: property), message))
    }
}
