package by.overpas.detekt.intellij.kotlin.otherproblems

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtAnnotationEntry

@IntellijInspection("MigrateDiagnosticSuppression")
class MigrateDiagnosticSuppression(config: Config) :
    Rule(
        config,
        "A `@Suppress` annotation uses an old diagnostic name that the compiler no longer reports. " +
            "Replace it with the new diagnostic name.",
    ),
    RequiresAnalysisApi {

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        super.visitAnnotationEntry(annotationEntry)
        if (annotationEntry.calleeExpression?.text != "Suppress") return
        val classId = analyze(annotationEntry) { annotationEntry.typeReference?.type?.expandedSymbol?.classId }
        if (classId != StandardClassIds.Annotations.Suppress) return
        annotationEntry.obsoleteDiagnosticNames().forEach { (expression, newName) ->
            report(
                Finding(
                    Entity.from(expression),
                    "Diagnostic name should be replaced by the new one '$newName'",
                ),
            )
        }
    }
}
