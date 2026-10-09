package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.psi.KtClass

private val DATA_CLASS_PRIVATE_CONSTRUCTOR_COPY_ANNOTATIONS = setOf(
    "ConsistentCopyVisibility",
    "ExposedCopyVisibility",
)

private val DATA_CLASS_PRIVATE_CONSTRUCTOR_COPY_FEATURES = listOf(
    LanguageFeature.ErrorAboutDataClassCopyVisibilityChange,
    LanguageFeature.DataClassCopyRespectsConstructorVisibility,
)

internal val KtClass.isDataClassPrivateConstructorExposed: Boolean
    get() = isData() &&
        annotationEntries.none { it.shortName?.asString() in DATA_CLASS_PRIVATE_CONSTRUCTOR_COPY_ANNOTATIONS } &&
        analyze(this) {
            val settings = (useSiteModule as? KaSourceModule)?.languageVersionSettings
            DATA_CLASS_PRIVATE_CONSTRUCTOR_COPY_FEATURES.none { settings?.supportsFeature(it) == true }
        }
