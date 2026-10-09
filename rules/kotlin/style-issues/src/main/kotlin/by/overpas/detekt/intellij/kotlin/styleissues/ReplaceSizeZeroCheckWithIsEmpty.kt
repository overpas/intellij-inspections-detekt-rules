package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression

class ReplaceSizeZeroCheckWithIsEmpty(config: Config) :
    Rule(
        config,
        "A check that a size or a length is zero is verbose. Call `isEmpty()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val sizeCall = expression.sizeZeroCheckWithIsEmptyTarget() ?: return
        if (!expression.isInsideSizeCheckReplacement("isEmpty") && sizeCall.isReplaceableSizeCheckCall()) {
            report(Finding(Entity.from(expression), "Size zero check can be replaced with 'isEmpty()'"))
        }
    }
}
