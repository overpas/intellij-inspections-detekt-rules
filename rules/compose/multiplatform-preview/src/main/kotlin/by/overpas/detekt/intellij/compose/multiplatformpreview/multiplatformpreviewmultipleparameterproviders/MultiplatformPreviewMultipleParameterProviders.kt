package by.overpas.detekt.intellij.compose.multiplatformpreview.multiplatformpreviewmultipleparameterproviders

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.compose.multiplatformpreview.hasActiveAndroidCounterpart
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtNamedFunction

@IntellijInspection("MultiplatformPreviewMultipleParameterProviders")
class MultiplatformPreviewMultipleParameterProviders(config: Config) :
    Rule(
        config,
        "A function with a Jetpack or Compose Multiplatform `@Preview`, or a `@Composable` function with a " +
            "MultiPreview annotation, has more than one `@PreviewParameter`. Compose Preview supports only one " +
            "`@PreviewParameter`. Remove the extra `@PreviewParameter` annotations.",
    ),
    RequiresAnalysisApi {

    private val areJetpackPreviewsCoveredByAndroid = hasActiveAndroidCounterpart()

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        analyze(function) {
            function.secondPreviewParameter()
                ?.takeUnless { areJetpackPreviewsCoveredByAndroid && function.hasJetpackMultipleProviders() }
        }?.let { annotation ->
            report(Finding(Entity.from(annotation), "Multiple @PreviewParameter are not allowed"))
        }
    }
}
