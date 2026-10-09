package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.diagnostics.KaSeverity
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry

@IntellijInspection("RedundantEnumConstructorInvocation")
class RedundantEnumConstructorInvocation(config: Config) :
    Rule(
        config,
        "Empty parentheses after an enum entry call the default constructor, which happens anyway. " +
            "Remove the parentheses.",
    ),
    RequiresAnalysisApi {

    override fun visitEnumEntry(enumEntry: KtEnumEntry) {
        super.visitEnumEntry(enumEntry)
        val callEntry = enumEntry.superTypeListEntries.singleOrNull() as? KtSuperTypeCallEntry ?: return
        val arguments = callEntry.valueArgumentList ?: return
        if (arguments.arguments.isEmpty() && !callEntry.hasErrors()) {
            report(Finding(Entity.from(arguments), "Redundant enum constructor invocation"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtSuperTypeCallEntry.hasErrors(): Boolean =
        analyze(this) {
            directDiagnostics(KaDiagnosticCheckerFilter.ONLY_COMMON_CHECKERS).any { it.severity == KaSeverity.ERROR }
        }
}
