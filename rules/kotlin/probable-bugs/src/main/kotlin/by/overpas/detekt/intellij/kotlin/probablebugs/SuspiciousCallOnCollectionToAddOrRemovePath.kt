package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression

@IntellijInspection("SuspiciousCallOnCollectionToAddOrRemovePath")
class SuspiciousCallOnCollectionToAddOrRemovePath(config: Config) :
    Rule(
        config,
        "A 'plus' or 'minus' call with an argument that is iterable over its own type, such as a 'Path', " +
            "adds or removes each of its elements instead of the argument itself. " +
            "Use 'plusElement' or 'minusElement', or convert the argument to an explicit collection.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operation = expression.operationReference
        val call = SuspiciousCallOnCollectionToAddOrRemovePathCall(expression)
        val isPlusOrMinus = expression.operationToken == KtTokens.PLUS || expression.operationToken == KtTokens.MINUS
        if (isPlusOrMinus && call.isSuspicious()) {
            report(Finding(Entity.from(operation), call.message(isPlus = expression.operationToken == KtTokens.PLUS)))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression
        val call = SuspiciousCallOnCollectionToAddOrRemovePathCall(expression)
        if (callee != null && callee.text in setOf("plus", "minus") && call.isSuspicious()) {
            report(Finding(Entity.from(callee), call.message(isPlus = callee.text == "plus")))
        }
    }
}
