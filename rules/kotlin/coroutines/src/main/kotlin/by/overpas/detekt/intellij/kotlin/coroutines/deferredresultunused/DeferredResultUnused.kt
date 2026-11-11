package by.overpas.detekt.intellij.kotlin.coroutines.deferredresultunused

import by.overpas.detekt.intellij.IntellijInspection
import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.psi.KtValueArgument

@IntellijInspection("DeferredResultUnused")
class DeferredResultUnused(config: Config) :
    Rule(
        config,
        "A call that returns a `Deferred` is useless when its result is never used. " +
            "Await the result, or use `launch` when no result is needed.",
    ),
    RequiresAnalysisApi {

    @Configuration("Report only the calls of `kotlinx.coroutines.async`, not every call that returns a `Deferred`.")
    private val isStandardOnly: Boolean by config(false)

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (!expression.isConsumedByParent() && DeferredResultUnusedCall(expression, isStandardOnly).isDiscarded()) {
            report(Finding(Entity.from(expression.calleeExpression ?: expression), "'Deferred' result is unused"))
        }
    }

    private fun KtCallExpression.isConsumedByParent(): Boolean =
        generateSequence<PsiElement>(this) { it.parent }
            .zipWithNext()
            .takeWhile { (_, parent) -> parent !is KtBlockExpression && parent !is KtFunction && parent !is KtFile }
            .any { (current, parent) ->
                parent is KtValueArgument ||
                    parent is KtBinaryExpression ||
                    parent is KtUnaryExpression ||
                    (parent as? KtQualifiedExpression)?.receiverExpression == current
            }
}
