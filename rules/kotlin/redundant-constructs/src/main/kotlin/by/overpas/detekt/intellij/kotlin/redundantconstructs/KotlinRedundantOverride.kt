package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtNamedFunction

class KotlinRedundantOverride(config: Config) :
    Rule(
        config,
        "An override that only calls the super implementation with the same arguments does nothing. " +
            "Remove the override.",
    ),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val superCall = function.superQualifiedExpression()?.superCallElement() ?: return
        val overrideModifier = function.modifierList?.getModifier(KtTokens.OVERRIDE_KEYWORD)
        val isCandidate = overrideModifier != null &&
            function.hasOnlyOverrideModifier() &&
            !function.hasNonSuppressAnnotations() &&
            function.isSameNameAndArguments(superCall)
        if (isCandidate && function.isRedundantOverrideOf(superCall)) {
            report(Finding(Entity.from(overrideModifier), "Redundant overriding method"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtNamedFunction.isRedundantOverrideOf(superCall: KtCallElement): Boolean {
        val function = this
        return analyze(function) {
            val superSignature = superCall.resolveCall()?.signature
            val symbol = function.symbol
            val overridden = symbol.allOverriddenSymbols.toList()
            superSignature != null &&
                !isDataClassAnyMember(function, superSignature.symbol) &&
                !hasDerivedProperty(function, symbol) &&
                parametersMatch(symbol, superSignature) &&
                !(superSignature.symbol.isAnyMember() && overridden.any { it.modality == KaSymbolModality.ABSTRACT }) &&
                overridden.none { it.isPackageVisibleJavaSymbol() } &&
                !isAmbiguouslyDerived(overridden) &&
                !implementsDelegatedMember(function, overridden)
        }
    }
}
