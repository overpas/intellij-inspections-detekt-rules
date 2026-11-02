package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class BlockingMethodInNonBlockingContextContext(
    private val expression: KtCallExpression,
    private val annotations: BlockingMethodInNonBlockingContextAnnotations,
    private val isSuspendContextNonBlocking: Boolean,
    private val blockingLambdaFunctions: List<String>,
) {

    context(session: KaSession)
    fun type(): BlockingMethodInNonBlockingContextType {
        val coroutine = BlockingMethodInNonBlockingContextCoroutine(
            expression,
            isSuspendContextNonBlocking,
            blockingLambdaFunctions,
        ).type()
        return if (coroutine == BlockingMethodInNonBlockingContextType.NON_BLOCKING) {
            coroutine
        } else {
            maxOf(coroutine, annotated())
        }
    }

    context(session: KaSession)
    private fun annotated(): BlockingMethodInNonBlockingContextType =
        with(session) {
            val method = expression.parents.filterIsInstance<KtDeclaration>().firstOrNull {
                (it is KtNamedFunction && !it.isLocal) || it is KtPropertyAccessor || it is KtConstructor<*>
            }
            val owner = method?.containingClassOrObject?.classSymbol
            method?.let { annotations.classify(it.symbol, owner) } ?: BlockingMethodInNonBlockingContextType.UNSURE
        }
}
