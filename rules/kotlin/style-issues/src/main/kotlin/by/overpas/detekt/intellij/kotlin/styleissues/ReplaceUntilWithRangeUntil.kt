package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression

class ReplaceUntilWithRangeUntil(config: Config) :
    Rule(
        config,
        "The infix function `until` from the standard library has an operator form. Use the `..<` operator instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.operationReference.getReferencedName() == "until" && expression.isKotlinCall()) {
            report(Finding(Entity.from(expression), "'until' can be replaced with '..<' operator"))
        }
    }

    private fun KtBinaryExpression.isKotlinCall(): Boolean =
        analyze(this) {
            val packageName = resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }?.packageName
            packageName?.startsWith(Name.identifier("kotlin")) == true
        }
}
