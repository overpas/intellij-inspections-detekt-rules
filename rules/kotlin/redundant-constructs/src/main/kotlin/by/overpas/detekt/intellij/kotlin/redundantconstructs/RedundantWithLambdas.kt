package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaMultiCall
import org.jetbrains.kotlin.analysis.api.resolution.KaReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleCall
import org.jetbrains.kotlin.analysis.api.resolution.KaSingleOrMultiCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

private val WITH_FQ_NAME = FqName("kotlin.with")

internal fun KtValueArgument.withLambdaExpression(): KtLambdaExpression? =
    (this as? KtLambdaArgument)?.getLambdaExpression() ?: getArgumentExpression() as? KtLambdaExpression

internal fun KtCallExpression.isWithFunctionBody(): Boolean =
    getStrictParentOfType<KtFunction>()?.bodyExpression?.let { KtPsiUtil.safeDeparenthesize(it) } == this

context(session: KaSession)
internal fun KtCallExpression.isKotlinWithCall(): Boolean {
    val function = with(session) { resolveToCall()?.successfulFunctionCallOrNull()?.symbol }
    return function?.callableId?.asSingleFqName() == WITH_FQ_NAME
}

context(session: KaSession)
internal fun KtFunctionLiteral.usesWithReceiver(): Boolean {
    val owner = with(session) { symbol }
    return anyDescendantOfType<KtElement> {
        (it as? KtReturnExpression)?.getLabelName() == WITH_NAME || it.usesReceiverOf(owner)
    }
}

@OptIn(KaExperimentalApi::class)
internal fun KaSingleOrMultiCall?.singleCalls(): List<KaSingleCall<*, *>> =
    when (this) {
        is KaSingleCall<*, *> -> listOf(this)
        is KaMultiCall -> calls
        null -> emptyList()
    }

@OptIn(KaExperimentalApi::class)
internal fun KaSingleCall<*, *>.receiverValues(): List<KaReceiverValue> =
    listOfNotNull(dispatchReceiver, extensionReceiver) + contextArguments
