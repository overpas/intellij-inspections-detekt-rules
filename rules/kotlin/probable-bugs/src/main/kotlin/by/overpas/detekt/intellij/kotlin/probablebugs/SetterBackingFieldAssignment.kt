package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

class SetterBackingFieldAssignment(config: Config) :
    Rule(
        config,
        "The setter of a property with a backing field never assigns the field, so the new value is lost. " +
            "Assign the value to 'field' in the setter.",
    ),
    RequiresAnalysisApi {

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        val parameter = accessor.valueParameters.singleOrNull()
        val body = accessor.bodyBlockExpression
        if (!accessor.isSetter || parameter == null || body == null) return
        if (body.firstStatement is KtThrowExpression || accessor.property.getter?.hasBody() == true) return
        val isAssigned = body.anyDescendantOfType<KtExpression> { expression ->
            val statement = SetterBackingFieldAssignmentStatement(expression)
            statement.updates(accessor.property) || statement.forwards(parameter)
        }
        if (!isAssigned) {
            report(Finding(Entity.from(accessor), "Existing backing field is not assigned by the setter"))
        }
    }
}
