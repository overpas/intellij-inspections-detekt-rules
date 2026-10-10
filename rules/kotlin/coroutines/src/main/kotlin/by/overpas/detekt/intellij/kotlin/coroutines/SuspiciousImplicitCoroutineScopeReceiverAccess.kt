package by.overpas.detekt.intellij.kotlin.coroutines

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

@IntellijInspection("SuspiciousImplicitCoroutineScopeReceiverAccess")
class SuspiciousImplicitCoroutineScopeReceiverAccess(config: Config) :
    Rule(
        config,
        "A call inside a suspending lambda or function uses an outer `CoroutineScope` as its implicit receiver. " +
            "The call likely meant another scope. Use a labeled `this` to make the receiver explicit.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        expression.reportIfSuspicious()
    }

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        super.visitSimpleNameExpression(expression)
        if (expression.parent !is KtCallExpression) expression.reportIfSuspicious()
    }

    private fun KtExpression.reportIfSuspicious() {
        if (getQualifiedExpressionForSelector() != null) return
        val access = this
        val isSuspicious = analyze(access) {
            SuspiciousImplicitCoroutineScopeReceiverAccessCall(this, access).isSuspicious()
        }
        if (isSuspicious) {
            report(
                Finding(
                    Entity.from(access),
                    "Suspicious implicit 'CoroutineScope' receiver access in suspending context",
                ),
            )
        }
    }
}
