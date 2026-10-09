package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

class RedundantCompanionReference(config: Config) :
    Rule(
        config,
        "An explicit reference to a companion object is redundant when its members resolve without it. " +
            "Remove the companion reference.",
    ),
    RequiresAnalysisApi {

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        super.visitSimpleNameExpression(expression)
        val isRedundant = expression.isCompanionReferenceCandidate() &&
            expression.isCompanionNamedAsReference() &&
            expression.keepsCallTargetWithoutIt()
        if (isRedundant) report(Finding(Entity.from(expression), "Redundant Companion reference"))
    }
}
