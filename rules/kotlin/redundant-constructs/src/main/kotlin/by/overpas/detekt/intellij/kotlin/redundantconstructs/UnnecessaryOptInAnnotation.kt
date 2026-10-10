package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

@IntellijInspection("UnnecessaryOptInAnnotation")
class UnnecessaryOptInAnnotation(config: Config) :
    Rule(
        config,
        "An `@OptIn` annotation or marker is redundant when no experimental API that requires it is used. " +
            "Remove the annotation or the marker.",
    ),
    RequiresAnalysisApi {

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        super.visitAnnotationEntry(annotationEntry)
        val owner = annotationEntry.getStrictParentOfType<KtAnnotated>()
        if (owner == null || annotationEntry.valueArguments.isEmpty() || !annotationEntry.hasOptInName()) return
        val unused = analyze(annotationEntry) {
            val markers = UnnecessaryOptInAnnotationMarkers(this, annotationEntry, owner)
            if (markers.isOptIn()) markers.unusedMarkers() else emptyList()
        }
        when {
            unused.isEmpty() -> Unit

            unused.size == annotationEntry.valueArguments.size -> report(
                Finding(
                    Entity.from(annotationEntry),
                    "The opt-in annotation is redundant: no matching experimental API is used",
                ),
            )

            else -> unused.forEach { (expression, classId) ->
                report(
                    Finding(
                        Entity.from(expression),
                        "The opt-in marker is redundant: no experimental API marked with " +
                            "'${classId.shortClassName.asString()}' is used",
                    ),
                )
            }
        }
    }

    private fun KtAnnotationEntry.hasOptInName(): Boolean {
        val entryName = shortName?.asString() ?: return true
        val aliases = containingKtFile.importDirectives
            .filter { directive -> OPT_IN_CLASS_IDS.any { it.asSingleFqName() == directive.importedFqName } }
            .mapNotNull { it.aliasName }
        return OPT_IN_CLASS_IDS.any { it.shortClassName.asString() == entryName } || entryName in aliases
    }
}
