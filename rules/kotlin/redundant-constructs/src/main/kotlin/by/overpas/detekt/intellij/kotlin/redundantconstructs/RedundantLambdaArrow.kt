package by.overpas.detekt.intellij.kotlin.redundantconstructs

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtLambdaExpression

@IntellijInspection("RedundantLambdaArrow")
class RedundantLambdaArrow(config: Config) :
    Rule(
        config,
        "A lambda arrow without parameters or with only the implicit `it` parameter is redundant. Remove it.",
    ),
    RequiresAnalysisApi {

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)
        val arrow = lambdaExpression.functionLiteral.arrow ?: return
        val isRedundant = lambdaExpression.hasRemovableArrowParameters() &&
            lambdaExpression.hasOnlyOwnItReferences() &&
            lambdaExpression.keepsResolvedCallsWithoutArrow()
        if (isRedundant) report(Finding(Entity.from(arrow), "Redundant lambda arrow"))
    }
}
