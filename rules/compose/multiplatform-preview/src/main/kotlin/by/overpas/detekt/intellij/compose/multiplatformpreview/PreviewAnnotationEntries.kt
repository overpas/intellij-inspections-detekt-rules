package by.overpas.detekt.intellij.compose.multiplatformpreview

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction

context(session: KaSession)
internal fun KtNamedFunction.previewAnnotationEntries(): List<KtAnnotationEntry> {
    val isComposable = isComposable()
    return annotationEntries.filter { entry ->
        when (val classId = entry.resolvedClassId()) {
            null -> false
            in PREVIEWS -> true
            else -> isComposable && classId.isMultiPreview()
        }
    }
}

context(session: KaSession)
internal fun KtAnnotationEntry.isJetpackPreviewOrMultiPreview(): Boolean =
    resolvedClassId()?.isJetpackPreviewOrMultiPreview() == true

context(session: KaSession)
private fun KtNamedFunction.isComposable(): Boolean =
    annotationEntries.any { it.resolvedClassId() == COMPOSABLE }
