package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtProperty

class ConflictingExtensionProperty(config: Config) :
    Rule(
        config,
        "An extension property with the name of a synthetic Java property of its receiver is shadowed by it. " +
            "Remove or rename the extension property.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (property.receiverTypeReference == null || property.nameIdentifier == null) return
        if (analyze(property) { property.conflictsWithSyntheticProperty() }) {
            report(
                Finding(
                    Entity.atName(property),
                    "Property conflicts with a synthetic extension and should be removed or renamed to avoid " +
                        "breaking code by future versions of the Kotlin compiler",
                ),
            )
        }
    }
}
