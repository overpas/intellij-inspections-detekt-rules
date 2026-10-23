package by.overpas.detekt.intellij.compose.multiplatformpreview.multiplatformpreviewmultipleparameterproviders

import by.overpas.detekt.intellij.compose.multiplatformpreview.JETPACK_PREVIEW_PARAMETER
import by.overpas.detekt.intellij.compose.multiplatformpreview.isJetpackPreviewOrMultiPreview
import by.overpas.detekt.intellij.compose.multiplatformpreview.previewAnnotationEntries
import by.overpas.detekt.intellij.compose.multiplatformpreview.previewParameterAnnotation
import by.overpas.detekt.intellij.compose.multiplatformpreview.resolvedClassId
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction

context(session: KaSession)
internal fun KtNamedFunction.secondPreviewParameter(): KtAnnotationEntry? =
    valueParameters.mapNotNull { it.previewParameterAnnotation() }
        .getOrNull(1)
        ?.takeIf { previewAnnotationEntries().isNotEmpty() }

context(session: KaSession)
internal fun KtNamedFunction.hasJetpackMultipleProviders(): Boolean =
    valueParameters.count { parameter ->
        parameter.annotationEntries.any { it.resolvedClassId() == JETPACK_PREVIEW_PARAMETER }
    } > 1 && annotationEntries.any { it.isJetpackPreviewOrMultiPreview() }
