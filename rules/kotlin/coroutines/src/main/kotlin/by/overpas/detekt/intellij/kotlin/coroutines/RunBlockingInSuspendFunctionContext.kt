package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

internal class RunBlockingInSuspendFunctionContext(
    private val session: KaSession,
    private val element: KtElement,
) {

    fun isSuspend(): Boolean =
        with(session) {
            val owner = element.parents
                .filterIsInstance<KtFunction>()
                .firstOrNull { !RunBlockingInSuspendFunctionLambda(session, it).isInlined() }
            when {
                owner == null -> false

                owner is KtFunctionLiteral || (owner is KtNamedFunction && owner.isAnonymous) ->
                    RunBlockingInSuspendFunctionLambda(session, owner).expression().expressionType
                        ?.isSuspendFunctionType == true

                else -> owner.hasModifier(KtTokens.SUSPEND_KEYWORD)
            }
        }
}
