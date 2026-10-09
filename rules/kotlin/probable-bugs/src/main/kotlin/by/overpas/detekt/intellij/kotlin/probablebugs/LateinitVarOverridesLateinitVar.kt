package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaKotlinPropertySymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty

class LateinitVarOverridesLateinitVar(config: Config) :
    Rule(
        config,
        "A `lateinit var` property that overrides a `lateinit var` property creates two backing fields. " +
            "Remove the override or make the base property abstract.",
    ),
    RequiresAnalysisApi {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (!property.isVar || !property.hasModifier(KtTokens.OVERRIDE_KEYWORD)) return
        if (!property.hasModifier(KtTokens.LATEINIT_KEYWORD)) return
        val isOverridingLateinitVar = analyze(property) {
            property.symbol.allOverriddenSymbols
                .filterIsInstance<KaKotlinPropertySymbol>()
                .any { !it.isVal && it.isLateInit }
        }
        if (isOverridingLateinitVar) {
            report(Finding(Entity.atName(property), "'lateinit var' overrides super 'lateinit var'"))
        }
    }
}
