package by.overpas.detekt.intellij.compose.multiplatformpreview.multiplatformpreviewannotationinfunctionwithparameters

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.compose.multiplatformpreview.hasActiveAndroidCounterpart
import by.overpas.detekt.intellij.compose.multiplatformpreview.isJetpackPreviewOrMultiPreview
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction

@IntellijInspection("MultiplatformPreviewAnnotationInFunctionWithParameters")
class MultiplatformPreviewAnnotationInFunctionWithParameters(config: Config) :
    Rule(
        config,
        "A function with a Jetpack or Compose Multiplatform `@Preview`, or a `@Composable` function with a " +
            "MultiPreview annotation, has a parameter without a default value and without `@PreviewParameter`. " +
            "Compose Preview cannot supply a value for this parameter. Add a default value or `@PreviewParameter` " +
            "to the parameter.",
    ),
    RequiresAnalysisApi {

    private val areJetpackPreviewsCoveredByAndroid = hasActiveAndroidCounterpart()

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        analyze(function) {
            function.unsupportedParameterPreviews()
                .filterNot { areJetpackPreviewsCoveredByAndroid && it.isJetpackPreviewOrMultiPreview() }
        }.forEach { annotation ->
            report(
                Finding(
                    Entity.from(annotation),
                    "Composable functions with non-default parameters are not supported in Preview unless they " +
                        "are annotated with @PreviewParameter",
                ),
            )
        }
    }
}
