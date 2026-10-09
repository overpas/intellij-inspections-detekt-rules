package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtProperty

class CanBePrimaryConstructorProperty(config: Config) :
    Rule(
        config,
        "A property that is only initialized with the constructor parameter of the same name and type " +
            "duplicates that parameter. Declare the property in the primary constructor.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val candidate = CanBePrimaryConstructorPropertyCandidate(property)
        if (candidate.isMovableToConstructor() && candidate.isAssignedFromSameParameter()) {
            report(
                Finding(
                    Entity.from(property.nameIdentifier ?: property),
                    "Property is explicitly assigned to parameter ${property.name.orEmpty()}, " +
                        "so it can be declared directly in the constructor",
                ),
            )
        }
    }
}
