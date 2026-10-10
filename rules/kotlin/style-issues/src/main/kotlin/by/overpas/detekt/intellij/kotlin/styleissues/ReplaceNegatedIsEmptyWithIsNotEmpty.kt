package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtPrefixExpression

@IntellijInspection("ReplaceNegatedIsEmptyWithIsNotEmpty")
class ReplaceNegatedIsEmptyWithIsNotEmpty(config: Config) :
    Rule(
        config,
        "A negated emptiness check such as `!list.isEmpty()` has a direct counterpart. " +
            "Call the inverted function, for example `list.isNotEmpty()`.",
    ),
    RequiresAnalysisApi {

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        val call = expression.negatedEmptinessCall() ?: return
        val name = call.calleeExpression?.text.orEmpty()
        val (functions, inverted) = REPLACE_NEGATED_IS_EMPTY_FUNCTIONS[name] ?: return
        val fqName = analyze(call) {
            val function = call.resolveToCall()?.successfulFunctionCallOrNull()
            function?.run { symbol.callableId }?.asSingleFqName()
        }
        if (fqName in functions) {
            report(Finding(Entity.from(expression), "Replace negated '$name' with '$inverted'"))
        }
    }
}
