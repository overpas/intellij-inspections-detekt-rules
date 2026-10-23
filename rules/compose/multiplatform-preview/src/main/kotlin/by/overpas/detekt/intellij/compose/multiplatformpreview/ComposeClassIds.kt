package by.overpas.detekt.intellij.compose.multiplatformpreview

import org.jetbrains.kotlin.name.ClassId

internal val COMPOSABLE = ClassId.fromString("androidx/compose/runtime/Composable")
internal val JETPACK_PREVIEW = ClassId.fromString("androidx/compose/ui/tooling/preview/Preview")
internal val JETPACK_PREVIEW_PARAMETER = ClassId.fromString("androidx/compose/ui/tooling/preview/PreviewParameter")
internal val PREVIEWS = setOf(
    JETPACK_PREVIEW,
    ClassId.fromString("org/jetbrains/compose/ui/tooling/preview/Preview"),
)
internal val PREVIEW_PARAMETERS = setOf(
    JETPACK_PREVIEW_PARAMETER,
    ClassId.fromString("org/jetbrains/compose/ui/tooling/preview/PreviewParameter"),
)
