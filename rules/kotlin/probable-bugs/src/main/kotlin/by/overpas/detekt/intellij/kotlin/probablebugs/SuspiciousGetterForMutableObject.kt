package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtPropertyAccessor

@IntellijInspection("SuspiciousGetterForMutableObject")
class SuspiciousGetterForMutableObject(config: Config) :
    Rule(
        config,
        "A getter that creates a new mutable object returns a different object on each access, " +
            "so changes to it are lost. Store the object in a property initializer.",
    ),
    RequiresAnalysisApi {

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        val call = accessor.suspiciousGetterFactoryCall() ?: return
        analyze(call) { call.suspiciousGetterMessage(accessor.property) }
            ?.let { report(Finding(Entity.from(accessor), it)) }
    }
}
