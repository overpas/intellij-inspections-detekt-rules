package by.overpas.detekt.intellij.kotlin.numericissues

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private const val KOTLIN_BIG_DECIMAL_EQUALS_MESSAGE =
    "'equals()' between BigDecimal values should probably be 'compareTo()'"

@IntellijInspection("KotlinBigDecimalEquals")
class KotlinBigDecimalEquals(config: Config) :
    Rule(
        config,
        "`equals()` on `BigDecimal` also compares the scale, so `1.0` and `1.00` are not equal. " +
            "Use `compareTo()` to compare the values.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val isEquality = expression.operationToken == KtTokens.EQEQ || expression.operationToken == KtTokens.EXCLEQ
        if (isEquality && analyze(expression) { expression.isBigDecimalEquality() }) {
            report(Finding(Entity.from(expression), KOTLIN_BIG_DECIMAL_EQUALS_MESSAGE))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isEqualsCall = expression.calleeExpression?.text == KOTLIN_BIG_DECIMAL_EQUALS_NAME &&
            expression.valueArguments.size == 1 &&
            expression.parent is KtQualifiedExpression
        if (isEqualsCall && analyze(expression) { expression.isBigDecimalEqualsCall() }) {
            report(Finding(Entity.from(expression), KOTLIN_BIG_DECIMAL_EQUALS_MESSAGE))
        }
    }
}
