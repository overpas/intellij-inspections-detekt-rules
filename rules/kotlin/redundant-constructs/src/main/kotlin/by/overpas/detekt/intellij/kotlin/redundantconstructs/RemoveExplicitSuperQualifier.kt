package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtSuperExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver

class RemoveExplicitSuperQualifier(config: Config) :
    Rule(
        config,
        "A supertype qualifier on `super` is unnecessary when the call resolves to the same member without it. " +
            "Remove the qualifier.",
    ),
    RequiresAnalysisApi {

    override fun visitSuperExpression(expression: KtSuperExpression) {
        super.visitSuperExpression(expression)
        val qualifier = expression.superTypeQualifier ?: return
        val selector = expression.getQualifiedExpressionForReceiver()?.selectorExpression ?: return
        val original = selector.resolvedCallableId()
        if (original != null && expression.unqualifiedSuperSelector(selector.text)?.resolvedCallableId() == original) {
            report(Finding(Entity.from(qualifier), "Remove explicit supertype qualification"))
        }
    }
}
