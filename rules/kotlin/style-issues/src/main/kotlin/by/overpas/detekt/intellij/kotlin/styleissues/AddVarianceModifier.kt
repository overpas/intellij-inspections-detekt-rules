package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.types.Variance

class AddVarianceModifier(config: Config) :
    Rule(
        config,
        "The type parameter of the class is used only in input or only in output positions. " +
            "Declare it with the `in` or `out` variance.",
    ),
    RequiresAnalysisApi {

    override fun visitTypeParameter(parameter: KtTypeParameter) {
        super.visitTypeParameter(parameter)
        if (parameter.variance != Variance.INVARIANT) return
        val variance = analyze(parameter) { AddVarianceModifierUsages(this, parameter.symbol).suggestedVariance() }
        if (variance != null) report(Finding(Entity.from(parameter), "Type parameter can have '$variance' variance"))
    }
}
