package by.overpas.detekt.intellij.kotlin.styleissues.addoperatormodifier

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.canBeOperator
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.util.OperatorNameConventions

@IntellijInspection("AddOperatorModifier")
class AddOperatorModifier(config: Config) :
    Rule(
        config,
        "The function matches an operator convention but has no `operator` modifier. Add the `operator` modifier. " +
            "A `set` function that does not return `Unit` is not reported.",
    ),
    RequiresAnalysisApi {

    @Configuration("Do not report `suspend` functions.")
    private val areSuspendFunctionsSkipped: Boolean by config(true)

    @OptIn(KaExperimentalApi::class)
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.nameIdentifier == null || function.hasModifier(KtTokens.OPERATOR_KEYWORD)) return
        if (areSuspendFunctionsSkipped && function.hasModifier(KtTokens.SUSPEND_KEYWORD)) return
        val isOperatorCandidate = analyze(function) {
            (function.symbol as? KaNamedFunctionSymbol)?.run {
                val isNonUnitSet = name == OperatorNameConventions.SET && !returnType.isUnitType
                canBeOperator && !isOperator && !isNonUnitSet
            } == true
        }
        if (isOperatorCandidate) report(Finding(Entity.atName(function), "Function should have 'operator' modifier"))
    }
}
