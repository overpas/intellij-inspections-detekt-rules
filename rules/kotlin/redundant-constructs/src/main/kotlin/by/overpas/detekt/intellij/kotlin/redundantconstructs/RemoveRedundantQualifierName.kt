package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaIdeApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.ShortenOptions
import org.jetbrains.kotlin.psi.KtFile

@OptIn(KaIdeApi::class)
@IntellijInspection("RemoveRedundantQualifierName")
class RemoveRedundantQualifierName(config: Config) :
    Rule(
        config,
        "A qualifier that the code resolves the same without is redundant. Remove the qualifier.",
    ),
    RequiresAnalysisApi {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val elements = analyze(file) {
            val shortenings = collectPossibleReferenceShorteningsInElement(
                element = file,
                shortenOptions = SHORTEN_OPTIONS,
                classShortenStrategy = { qualifierClassStrategy(it) },
                callableShortenStrategy = { qualifierCallableStrategy(it) },
            )
            val qualifiers = shortenings.listOfQualifierToShortenInfo.mapNotNull { it.qualifierToShorten.element }
            val types = shortenings.listOfTypeToShortenInfo.mapNotNull { it.typeToShorten.element }
            (qualifiers + types).filter { keepsResolveUnqualified(it) }
        }
        elements.forEach { report(Finding(Entity.from(it), "Redundant qualifier name")) }
    }

    private companion object {
        val SHORTEN_OPTIONS = ShortenOptions(
            removeThis = false,
            removeThisLabels = false,
            removeContextSensitiveResolutionQualifiers = false,
        )
    }
}
