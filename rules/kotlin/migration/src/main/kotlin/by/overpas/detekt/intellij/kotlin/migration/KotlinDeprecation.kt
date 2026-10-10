package by.overpas.detekt.intellij.kotlin.migration

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtFile

@IntellijInspection("KotlinDeprecation")
class KotlinDeprecation(config: Config) :
    Rule(
        config,
        "The code uses deprecated syntax, deprecated symbols that have a replacement, or redundant constructs " +
            "that the compiler reports. Apply the replacement or remove the redundant construct.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val imports = analyze(file) { file.deprecatedImports() }
        imports.forEach { report(Finding(Entity.from(it), "Remove deprecated symbol import")) }
        val diagnostics = analyze(file) { file.cleanupDiagnostics() }
        diagnostics.forEach { (element, message) -> report(Finding(Entity.from(element), message)) }
    }
}
