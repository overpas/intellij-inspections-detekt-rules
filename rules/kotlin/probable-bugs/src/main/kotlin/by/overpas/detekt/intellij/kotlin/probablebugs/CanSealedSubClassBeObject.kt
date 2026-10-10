package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass

@IntellijInspection("CanSealedSubClassBeObject")
class CanSealedSubClassBeObject(config: Config) :
    Rule(
        config,
        "A subclass of a sealed class without state and without an own `equals()` creates equal-looking instances " +
            "that are compared by identity. Convert it to an object or override `equals()` and `hashCode()`.",
    ),
    RequiresAnalysisApi {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        val keyword = klass.getClassOrInterfaceKeyword() ?: return
        val candidate = CanSealedSubClassBeObjectCandidate(klass)
        if (candidate.isSingletonShaped() && candidate.extendsStatelessSealed()) {
            report(Finding(Entity.from(keyword), "'sealed' subclass has no state and no overridden 'equals()'"))
        }
    }
}
