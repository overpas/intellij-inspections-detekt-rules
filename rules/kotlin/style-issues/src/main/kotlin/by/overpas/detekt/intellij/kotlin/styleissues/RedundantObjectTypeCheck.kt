package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.psi.KtIsExpression

class RedundantObjectTypeCheck(config: Config) :
    Rule(
        config,
        "An `is` check against an object type is a reference check in disguise. Use `===` or `!==` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitIsExpression(expression: KtIsExpression) {
        super.visitIsExpression(expression)
        val typeReference = expression.typeReference ?: return
        val isObject = analyze(typeReference) {
            (typeReference.type.expandedSymbol as? KaNamedClassSymbol)?.run { classKind.isObject && !isData } == true
        }
        if (isObject) report(Finding(Entity.from(expression.operationReference), "Redundant type checks for object"))
    }
}
