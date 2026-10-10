package by.overpas.detekt.intellij.kotlin.javainterop

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

@IntellijInspection("JavaDefaultMethodsNotOverriddenByDelegation")
class JavaDefaultMethodsNotOverriddenByDelegation(config: Config) :
    Rule(
        config,
        "Delegation does not forward Java default methods, so the delegate's overrides of them are not used. " +
            "Override these methods explicitly.",
    ),
    RequiresAnalysisApi {

    override fun visitDelegatedSuperTypeEntry(specifier: KtDelegatedSuperTypeEntry) {
        super.visitDelegatedSuperTypeEntry(specifier)
        val declaration = specifier.getStrictParentOfType<KtClassOrObject>() ?: return
        val hasSkippedOverrides = analyze(specifier) { specifier.skipsDefaultMethodOverrides(declaration) }
        if (hasSkippedOverrides) {
            report(Finding(Entity.from(specifier), "Java default methods are not overridden by delegation"))
        }
    }
}
