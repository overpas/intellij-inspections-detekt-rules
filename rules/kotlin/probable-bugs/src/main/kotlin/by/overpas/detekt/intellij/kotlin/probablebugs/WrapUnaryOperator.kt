package by.overpas.detekt.intellij.kotlin.probablebugs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

private val wrapUnaryOperatorTokens = setOf(KtTokens.MINUS, KtTokens.PLUS)

private val wrapUnaryOperatorNumberTypes = setOf(KtNodeTypes.INTEGER_CONSTANT, KtNodeTypes.FLOAT_CONSTANT)

@IntellijInspection("WrapUnaryOperator")
class WrapUnaryOperator(config: Config) :
    Rule(
        config,
        "A unary minus or plus before a number literal with a call applies to the result of the call, not to the " +
            "literal. Wrap the operator and the literal in parentheses.",
    ),
    RequiresAnalysisApi {

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        val receiver = (expression.baseExpression as? KtDotQualifiedExpression)?.receiverExpression
        val isAmbiguous = expression.operationToken in wrapUnaryOperatorTokens &&
            receiver is KtConstantExpression &&
            receiver.node.elementType in wrapUnaryOperatorNumberTypes
        if (isAmbiguous) report(Finding(Entity.from(expression), "Ambiguous unary operator use with number constant"))
    }
}
