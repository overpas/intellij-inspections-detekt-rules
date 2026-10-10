package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

@IntellijInspection("SuspiciousCallableReferenceInLambda")
class SuspiciousCallableReferenceInLambda(config: Config) :
    Rule(
        config,
        "A lambda that only returns a callable reference is likely a mistake. Pass the reference instead.",
    ),
    RequiresAnalysisApi {

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        val statement = lambdaExpression.bodyExpression?.run { statements.singleOrNull() }
        if (statement !is KtCallableReferenceExpression) return
        val isSuspicious = analyze(lambdaExpression) {
            val callExpression = lambdaExpression.getStrictParentOfType<KtCallExpression>()
            val call = callExpression?.let { it.resolveToCall()?.successfulFunctionCallOrNull() }
            isSuspiciousCallContext(lambdaExpression, call) &&
                isSuspiciousUsageContext(lambdaExpression)
        }
        if (isSuspicious) {
            report(Finding(Entity.from(lambdaExpression), "Suspicious callable reference as the only lambda element"))
        }
    }
}
