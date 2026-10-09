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
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val REPLACE_MAP_GET_OR_DEFAULT_FQ_NAME = FqName("kotlin.collections.Map.getOrDefault")

@IntellijInspection("ReplaceMapGetOrDefault")
class ReplaceMapGetOrDefault(config: Config) :
    Rule(
        config,
        "`map.getOrDefault(key, default)` on a map with non-null values reads better as `map[key] ?: default`. " +
            "Replace the call with indexing and the elvis operator.",
    ),
    RequiresAnalysisApi {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val call = (expression.selectorExpression as? KtCallExpression)?.takeIf { call ->
            call.calleeExpression?.text == "getOrDefault" &&
                call.valueArguments.size == 2 &&
                call.valueArguments.all { it.getArgumentExpression() != null }
        } ?: return
        val receiver = expression.receiverExpression
        val isReplaceable = analyze(expression) {
            val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            val valueArgument = (receiver.expressionType as? KaClassType)?.run { typeArguments.lastOrNull() }
            symbol?.callableId?.asSingleFqName() == REPLACE_MAP_GET_OR_DEFAULT_FQ_NAME &&
                valueArgument != null &&
                valueArgument.type?.isMarkedNullable != true
        }
        if (isReplaceable) {
            val (key, default) = call.valueArguments.mapNotNull { it.getArgumentExpression()?.text }
            report(
                Finding(Entity.from(call.calleeExpression ?: call), "Replace with ${receiver.text}[$key] ?: $default"),
            )
        }
    }
}
