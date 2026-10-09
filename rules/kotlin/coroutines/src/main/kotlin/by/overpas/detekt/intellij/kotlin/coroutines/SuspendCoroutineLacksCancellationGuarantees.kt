package by.overpas.detekt.intellij.kotlin.coroutines

import by.overpas.detekt.intellij.IntellijInspection
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

@IntellijInspection("SuspendCoroutineLacksCancellationGuarantees")
class SuspendCoroutineLacksCancellationGuarantees(config: Config) :
    Rule(
        config,
        "`suspendCoroutine` ignores the cancellation of the coroutine. Use `suspendCancellableCoroutine` from " +
            "kotlinx.coroutines instead.",
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
        if (callee.getReferencedNameAsName() != SUSPEND_COROUTINE.callableName) return
        val isReported = analyze(expression) {
            expression.resolveToCall()?.successfulFunctionCallOrNull()?.run {
                symbol.callableId
            } == SUSPEND_COROUTINE &&
                findTopLevelCallables(COROUTINES_PACKAGE, SUSPEND_CANCELLABLE_COROUTINE).any()
        }
        if (isReported) report(Finding(Entity.from(callee), "'suspendCoroutine' lacks cancellation guarantees"))
    }

    private companion object {
        val SUSPEND_COROUTINE = CallableId(FqName("kotlin.coroutines"), Name.identifier("suspendCoroutine"))
        val COROUTINES_PACKAGE = FqName("kotlinx.coroutines")
        val SUSPEND_CANCELLABLE_COROUTINE = Name.identifier("suspendCancellableCoroutine")
    }
}
