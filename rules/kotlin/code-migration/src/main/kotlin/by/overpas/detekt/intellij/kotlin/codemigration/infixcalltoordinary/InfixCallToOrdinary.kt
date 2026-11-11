package by.overpas.detekt.intellij.kotlin.codemigration.infixcalltoordinary

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression

@IntellijInspection("InfixCallToOrdinary")
class InfixCallToOrdinary(config: Config) :
    Rule(
        config,
        "An infix call can be written as an ordinary call with a dot and parentheses. " +
            "Replace the infix call with an ordinary call where it reads better.",
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Do not report the infix calls of these functions, given as fully qualified names, e.g. `kotlin.to`.",
    )
    private val ignoredFunctions: Set<String> by config(emptyList<String>()) { it.toSet() }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val isInfixCall = expression.operationToken == KtTokens.IDENTIFIER &&
            expression.left != null &&
            expression.right != null
        if (isInfixCall && !expression.isIgnoredCall()) {
            report(Finding(Entity.from(expression.operationReference), "Replace infix call with ordinary call"))
        }
    }

    private fun KtBinaryExpression.isIgnoredCall(): Boolean =
        ignoredFunctions.isNotEmpty() && analyze(this) {
            val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            symbol?.callableId?.run { asSingleFqName().asString() } in ignoredFunctions
        }
}
