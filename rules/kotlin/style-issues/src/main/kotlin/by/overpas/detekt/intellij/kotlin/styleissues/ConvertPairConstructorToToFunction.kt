package by.overpas.detekt.intellij.kotlin.styleissues

import by.overpas.detekt.intellij.IntellijInspection
import by.overpas.detekt.intellij.OppositeRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression

private val CONVERT_PAIR_CONSTRUCTOR_CLASS_ID = ClassId(FqName("kotlin"), Name.identifier("Pair"))

@OppositeRule("InfixCallToOrdinary")
@IntellijInspection("ConvertPairConstructorToToFunction")
class ConvertPairConstructorToToFunction(config: Config) :
    Rule(
        config,
        "An explicit `Pair` constructor call reads better as the infix `to` function. " +
            "Replace `Pair(a, b)` with `a to b`.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression ?: return
        val arguments = expression.valueArguments
        val hasTwoArguments = arguments.size == 2 && arguments.all { it.getArgumentExpression() != null }
        if (callee.text != "Pair" || !hasTwoArguments) return
        val isPairConstructor = analyze(expression) {
            val symbol = expression.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            (symbol as? KaConstructorSymbol)?.containingClassId == CONVERT_PAIR_CONSTRUCTOR_CLASS_ID
        }
        if (isPairConstructor) {
            report(Finding(Entity.from(callee), "Explicit 'Pair' initiation can be replaced with an infix 'to()' call"))
        }
    }
}
