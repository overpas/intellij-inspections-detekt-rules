package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaImplicitInvokeCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtOperationExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

internal fun KtExpression.ifThenLeftMostReceiver(): KtExpression =
    generateSequence(this) { (it as? KtQualifiedExpression)?.receiverExpression }.last()

internal fun KtExpression.ifThenCallee(): KtExpression? =
    when (val expression = KtPsiUtil.deparenthesize(this)) {
        is KtSimpleNameExpression -> expression
        is KtCallExpression -> expression.calleeExpression
        is KtOperationExpression -> expression.operationReference
        else -> null
    }

internal fun KtExpression.ifThenParentsUpTo(root: KtExpression): Sequence<KtExpression> =
    generateSequence<PsiElement>(this) { element -> element.parent.takeUnless { element == root } }
        .filterIsInstance<KtExpression>()

@OptIn(KaExperimentalApi::class)
context(session: KaSession)
internal fun KtExpression.hasIfThenVariableCallsFrom(start: KtExpression): Boolean =
    with(session) {
        start.ifThenParentsUpTo(this@hasIfThenVariableCallsFrom)
            .mapNotNull { ((it as? KtQualifiedExpression)?.selectorExpression ?: it) as? KtCallExpression }
            .any { it.resolveCall() is KaImplicitInvokeCall }
    }
