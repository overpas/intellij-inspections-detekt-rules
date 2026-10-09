package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.canBeOperator
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

@IntellijInspection("AddOperatorModifier")
class AddOperatorModifier(config: Config) :
    Rule(
        config,
        "The function matches an operator convention but has no `operator` modifier. Add the `operator` modifier.",
    ),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.nameIdentifier == null || function.hasModifier(KtTokens.OPERATOR_KEYWORD)) return
        val isOperatorCandidate = analyze(function) {
            (function.symbol as? KaNamedFunctionSymbol)?.run { canBeOperator && !isOperator } == true
        }
        if (isOperatorCandidate) report(Finding(Entity.atName(function), "Function should have 'operator' modifier"))
    }
}
