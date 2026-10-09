package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

@IntellijInspection("DelegationToVarProperty")
class DelegationToVarProperty(config: Config) :
    Rule(
        config,
        "A class delegates to the value that a `var` property holds at construction time, so later changes of the " +
            "property have no effect. Make the property a `val`.",
    ),
    RequiresAnalysisApi {

    override fun visitDelegatedSuperTypeEntry(specifier: KtDelegatedSuperTypeEntry) {
        super.visitDelegatedSuperTypeEntry(specifier)
        val parameter = specifier.delegationToVarPropertyParameter ?: return
        val containingClass = parameter.containingClassOrObject as? KtClass ?: return
        if (!DelegationToVarPropertyReassignment(parameter, containingClass).isPresent) {
            report(
                Finding(Entity.from(parameter), "Delegating to 'var' property does not take its changes into account"),
            )
        }
    }
}
