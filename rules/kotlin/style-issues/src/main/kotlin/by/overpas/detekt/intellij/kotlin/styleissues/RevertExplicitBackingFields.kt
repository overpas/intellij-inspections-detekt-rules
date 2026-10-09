package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtProperty

class RevertExplicitBackingFields(config: Config) :
    Rule(
        config,
        "A property with an explicit backing field can be written with a private backing property. " +
            "Declare a private property and expose it through a getter.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val fieldKeyword = property.fieldDeclaration?.fieldKeyword ?: return
        report(Finding(Entity.from(fieldKeyword), "Replace explicit backing field with private property"))
    }
}
