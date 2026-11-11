package by.overpas.detekt.intellij.kotlin.coroutines.unusedflow

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

internal val UNUSED_FLOW_IGNORED_LAMBDA_FUNCTIONS = listOf(
    "verify",
    "coVerify",
    "verifyAll",
    "coVerifyAll",
    "verifyOrder",
    "coVerifyOrder",
    "verifySequence",
    "coVerifySequence",
    "excludeRecords",
).map { "io.mockk.$it" }

context(session: KaSession)
internal fun KtExpression.isInIgnoredLambda(functions: Set<String>): Boolean =
    with(session) {
        parents.filterIsInstance<KtLambdaExpression>()
            .mapNotNull { (it.parent as? KtValueArgument)?.getStrictParentOfType<KtCallExpression>() }
            .mapNotNull { it.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } }
            .any { it.asSingleFqName().asString() in functions }
    }
