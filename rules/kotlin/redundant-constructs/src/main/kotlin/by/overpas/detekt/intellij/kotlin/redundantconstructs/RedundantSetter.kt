package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtPropertyAccessor

class RedundantSetter(config: Config) :
    Rule(
        config,
        "A setter that only assigns the value to the backing field does nothing. Remove the setter or its body.",
    ),
    RequiresAnalysisApi {

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        if (!accessor.isRedundantSetterAccessor()) return
        val isDeletable = accessor.isSetterDeletable()
        val hasBody = accessor.bodyExpression != null
        val isWeaker = accessor.hasWeakerOverriddenSetter()
        if (!isWeaker || !isDeletable || hasBody) {
            val message = if (!isDeletable || (isWeaker && hasBody)) "Redundant setter body" else "Redundant setter"
            report(Finding(Entity.from(accessor), message))
        }
    }
}
