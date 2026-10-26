package by.overpas.detekt.intellij.kotlin.styleissues.booleanliteralargument

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

@IntellijInspection("BooleanLiteralArgument")
class BooleanLiteralArgument(config: Config) :
    Rule(
        config,
        "Adjacent `true` and `false` arguments without parameter names are hard to read. Name the arguments.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Do not report the arguments of these functions, given as fully qualified names, " +
            "e.g. `androidx.compose.ui.graphics.vector.PathBuilder.arcTo`.",
    )
    private val ignoredFunctions: List<String> by config(emptyList())

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val call = (argument.parent as? KtValueArgumentList)?.parent as? KtCallExpression ?: return
        val unnamedLiterals = call.valueArguments.map {
            !it.isNamed() && it.getArgumentExpression()?.node?.elementType == KtNodeTypes.BOOLEAN_CONSTANT
        }
        val index = call.valueArguments.indexOf(argument)
        val hasUnnamedLiteralNeighbour = unnamedLiterals.getOrNull(index - 1) == true ||
            unnamedLiterals.getOrNull(index + 1) == true
        if (unnamedLiterals.getOrNull(index) != true || !hasUnnamedLiteralNeighbour) return
        if (analyze(argument) { argument.hasBooleanLiteralStableName(call, ignoredFunctions) }) {
            report(Finding(Entity.from(argument), "Boolean literal argument without a parameter name"))
        }
    }
}
