package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

@IntellijInspection("NullChecksToSafeCall")
class NullChecksToSafeCall(config: Config) :
    Rule(
        config,
        "A chain of null checks on a receiver and on a call on it can be one null check of a safe call. " +
            "Replace the null checks with a safe call.",
    ),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val operation = expression.nullCheckOperation() ?: return
        val checked = (expression.left as? KtBinaryExpression)?.nullComparedExpression(operation)
        val qualified = (expression.right as? KtBinaryExpression)?.nullComparedExpression(operation)
        val isSameReceiver = qualified is KtQualifiedExpression &&
            checked?.hasSameTextIgnoringSafeCalls(qualified.receiverExpression) == true
        if (isSameReceiver && qualified.canBecomeSafeCall()) {
            report(Finding(Entity.from(expression), "Null-checks can be replaced with safe-calls"))
        }
    }

    @OptIn(KaExperimentalApi::class)
    private fun KtQualifiedExpression.canBecomeSafeCall(): Boolean {
        val qualified = this
        val stableCandidate = (receiverExpression as? KtThisExpression)?.instanceReference ?: receiverExpression
        return analyze(qualified) {
            val symbol = qualified.resolveSymbol()
            symbol != null &&
                stableCandidate.smartCastInfo?.isStable == true &&
                symbol.receiverParameter?.returnType.let { it == null || !it.isMarkedNullable }
        }
    }
}
