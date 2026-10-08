package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtTypeParameter

class RedundantUpperBound(config: Config) :
    Rule(
        config,
        "`Any?` is the default upper bound of a type parameter. Remove the explicit bound.",
    ),
    RequiresAnalysisApi {

    override fun visitTypeParameter(parameter: KtTypeParameter) {
        super.visitTypeParameter(parameter)
        val bound = parameter.extendsBound ?: return
        val isRedundant = analyze(bound) { bound.type.run { isAnyType && isMarkedNullable } }
        if (isRedundant) report(Finding(Entity.from(bound), "Redundant upper bound"))
    }
}
