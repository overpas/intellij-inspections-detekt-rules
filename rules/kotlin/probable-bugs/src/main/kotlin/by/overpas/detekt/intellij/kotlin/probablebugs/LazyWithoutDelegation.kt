package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtProperty

@IntellijInspection("LazyWithoutDelegation")
class LazyWithoutDelegation(config: Config) :
    Rule(
        config,
        "A private or local property holds a `Lazy` value and reads it only through `value`. " +
            "Delegate the property to the `Lazy` value with `by` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        val equalsToken = property.equalsToken ?: return
        if (property.isLazyWithoutDelegationCandidate && LazyWithoutDelegationUsages(property).areOnlyValueReads) {
            report(Finding(Entity.from(equalsToken), "'lazy' can be used with delegation"))
        }
    }
}
