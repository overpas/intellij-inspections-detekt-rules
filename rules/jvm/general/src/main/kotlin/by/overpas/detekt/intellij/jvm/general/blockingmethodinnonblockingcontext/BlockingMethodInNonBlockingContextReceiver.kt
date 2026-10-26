package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

private val COROUTINE_SCOPE_FACTORY = FqName("kotlinx.coroutines.CoroutineScope")

internal class BlockingMethodInNonBlockingContextReceiver(private val callExpression: KtExpression) {

    context(session: KaSession)
    fun dispatcher(): BlockingMethodInNonBlockingContextDispatcher =
        with(session) {
            val receiver = callExpression.getQualifiedExpressionForSelector()?.receiverExpression
            val factory = (receiver as? KtNameReferenceExpression)?.let { initializer(it) } ?: receiver
            val call = factory?.resolveToCall()?.successfulFunctionCallOrNull()
            if (factory != null && call?.symbol?.callableId?.asSingleFqName() == COROUTINE_SCOPE_FACTORY) {
                BlockingMethodInNonBlockingContextNested(factory).dispatcher()
            } else {
                BlockingMethodInNonBlockingContextDispatcher.NO_INFORMATION
            }
        }

    context(session: KaSession)
    private fun initializer(reference: KtNameReferenceExpression): KtExpression? =
        with(session) {
            val property = reference.mainReference.resolveToSymbol()?.psi as? KtProperty
            property?.takeIf { !it.isVar && (it.isLocal || it.isPrivate()) }?.initializer
        }
}
