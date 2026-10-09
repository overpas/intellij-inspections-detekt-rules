package by.overpas.detekt.intellij.kotlin.codemigration

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
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

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val isInfixCall = expression.operationToken == KtTokens.IDENTIFIER &&
            expression.left != null &&
            expression.right != null
        if (isInfixCall) {
            report(Finding(Entity.from(expression.operationReference), "Replace infix call with ordinary call"))
        }
    }
}
