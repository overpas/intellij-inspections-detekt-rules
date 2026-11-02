package by.overpas.detekt.intellij.kotlin.probablebugs.wrapunaryoperator

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaCallableMemberCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
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

    @Configuration(
        "Properties and functions after the literal to not report, given as fully qualified names, " +
            "e.g. `androidx.compose.ui.unit.dp`.",
    )
    private val ignoredSelectors: List<String> by config(emptyList())

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        val qualified = expression.baseExpression as? KtDotQualifiedExpression ?: return
        val receiver = qualified.receiverExpression
        val isAmbiguous = expression.operationToken in wrapUnaryOperatorTokens &&
            receiver is KtConstantExpression &&
            receiver.node.elementType in wrapUnaryOperatorNumberTypes
        if (isAmbiguous && !qualified.hasIgnoredSelector()) {
            report(Finding(Entity.from(expression), "Ambiguous unary operator use with number constant"))
        }
    }

    private fun KtDotQualifiedExpression.hasIgnoredSelector(): Boolean {
        val selector = selectorExpression ?: return false
        return ignoredSelectors.isNotEmpty() && analyze(selector) {
            val symbol = selector.resolveToCall()?.successfulCallOrNull<KaCallableMemberCall<*, *>>()?.symbol
            symbol?.callableId?.run { asSingleFqName().asString() } in ignoredSelectors
        }
    }
}
