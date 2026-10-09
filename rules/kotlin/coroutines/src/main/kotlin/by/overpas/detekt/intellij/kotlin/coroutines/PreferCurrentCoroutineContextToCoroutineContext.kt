package by.overpas.detekt.intellij.kotlin.coroutines

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

private const val MESSAGE = "Usage of 'kotlin.coroutine.coroutineContext' can be ambiguous"

@IntellijInspection("PreferCurrentCoroutineContextToCoroutineContext")
class PreferCurrentCoroutineContextToCoroutineContext(config: Config) :
    Rule(
        config,
        "The `kotlin.coroutines.coroutineContext` property can resolve to the context of a different scope. " +
            "Call `currentCoroutineContext()` instead.",
    ),
    RequiresAnalysisApi {

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        super.visitSimpleNameExpression(expression)
        val isAmbiguous = expression is KtNameReferenceExpression &&
            expression.getQualifiedExpressionForSelector() == null &&
            PreferCurrentCoroutineContextToCoroutineContextReference(expression).isAmbiguous()
        if (isAmbiguous) report(Finding(Entity.from(expression), MESSAGE))
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val selector = expression.selectorExpression as? KtNameReferenceExpression
        val isAmbiguous = selector != null &&
            expression.parent !is KtImportDirective &&
            PreferCurrentCoroutineContextToCoroutineContextReference(selector).isAmbiguous()
        if (isAmbiguous) report(Finding(Entity.from(expression), MESSAGE))
    }
}
