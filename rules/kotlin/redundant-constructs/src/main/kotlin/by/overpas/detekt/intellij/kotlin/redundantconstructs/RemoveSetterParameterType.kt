package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPropertyAccessor

@IntellijInspection("RemoveSetterParameterType")
class RemoveSetterParameterType(config: Config) :
    Rule(
        config,
        "The type of a setter parameter is always the type of the property. Remove the explicit type.",
    ) {

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        val isSetterParameter = (parameter.parent?.parent as? KtPropertyAccessor)?.isSetter == true
        val typeReference = parameter.typeReference
        if (isSetterParameter && typeReference != null && typeReference.textLength > 0) {
            report(Finding(Entity.from(typeReference), "Redundant setter parameter type"))
        }
    }
}
