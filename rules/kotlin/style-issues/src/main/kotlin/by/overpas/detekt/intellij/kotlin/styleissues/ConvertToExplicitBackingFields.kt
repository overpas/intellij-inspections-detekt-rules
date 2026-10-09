package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

@IntellijInspection("ConvertToExplicitBackingFields")
class ConvertToExplicitBackingFields(config: Config) :
    Rule(
        config,
        "A public property whose getter only returns a private backing property of a narrower type can use " +
            "an explicit backing field. Declare `field` in the property and remove the backing property.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val getter = property.getter ?: return
        if (property.isVar || property.isPrivate() || property.receiverTypeReference != null) return
        if (ConvertToExplicitBackingFieldsCandidate(property, getter).isConvertible) {
            report(Finding(Entity.from(getter), "Explicit backing fields can be used"))
        }
    }
}
