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
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

private const val RECURSIVE_EQUALS_CALL_MESSAGE = "Recursive equals call"

@IntellijInspection("RecursiveEqualsCall")
class RecursiveEqualsCall(config: Config) :
    Rule(
        config,
        "An `equals` implementation compares `this` with its parameter through `==` or `equals`, so it calls " +
            "itself again and again. Use the referential equality `===` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val isEquality = expression.operationToken == KtTokens.EQEQ || expression.operationToken == KtTokens.EXCLEQ
        val argument = expression.right as? KtNameReferenceExpression ?: return
        if (isEquality && RecursiveEqualsCallTarget(expression, argument).isRecursive) {
            report(Finding(Entity.from(expression), RECURSIVE_EQUALS_CALL_MESSAGE))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isEquals = (expression.calleeExpression as? KtSimpleNameExpression)?.getReferencedName() == "equals"
        val argument = expression.valueArguments.singleOrNull()?.getArgumentExpression() as? KtNameReferenceExpression
        if (isEquals && argument != null && RecursiveEqualsCallTarget(expression, argument).isRecursive) {
            report(Finding(Entity.from(expression), RECURSIVE_EQUALS_CALL_MESSAGE))
        }
    }
}
