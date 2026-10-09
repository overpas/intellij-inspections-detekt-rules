package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaSmartCastedReceiverValue
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis

internal class VerboseNullabilityAndEmptinessTarget(private val chunks: List<KtExpression>) {

    fun matchesByPsi(other: VerboseNullabilityAndEmptinessTarget): Boolean =
        chunks.size == other.chunks.size &&
            chunks.zip(other.chunks).all { (left, right) ->
                (left as? KtNameReferenceExpression)?.getReferencedName() ==
                    (right as? KtNameReferenceExpression)?.getReferencedName()
            }

    context(session: KaSession)
    fun matches(other: VerboseNullabilityAndEmptinessTarget): Boolean =
        chunks.zip(other.chunks).all { (left, right) ->
            left !is KtNameReferenceExpression || left.resolvedSymbol() == right.resolvedSymbol()
        }

    context(session: KaSession)
    fun hasSmartCast(): Boolean =
        with(session) {
            when (val chunk = chunks.last()) {
                is KtThisExpression ->
                    chunk.smartCastInfo != null ||
                        (chunk.parent as? KtDotQualifiedExpression)?.smartCastInfo != null ||
                        chunk.followsThisNullCheck()

                is KtCallExpression -> chunk.resolveToCall()?.successfulFunctionCallOrNull()
                    ?.let { (it.dispatchReceiver ?: it.extensionReceiver) is KaSmartCastedReceiverValue } == true

                else -> generateSequence(chunk.getQualifiedExpressionForSelectorOrThis()) { child ->
                    (child.parent as? KtExpression)?.takeIf { KtPsiUtil.deparenthesizeOnce(it) === child }
                }.any { it.smartCastInfo != null }
            }
        }

    private companion object {

        val logicalOperations = setOf(KtTokens.OROR, KtTokens.ANDAND)

        val nullityOperations = setOf(KtTokens.EQEQ, KtTokens.EXCLEQ)

        context(session: KaSession)
        fun KtExpression.resolvedSymbol(): Any? =
            with(session) { references.filterIsInstance<KtReference>().firstNotNullOfOrNull { it.resolveToSymbol() } }

        fun KtThisExpression.followsThisNullCheck(): Boolean {
            val check = (parent.parent as? KtBinaryExpression)
                ?.takeIf { parent is KtDotQualifiedExpression && it.operationToken in logicalOperations }
                ?.left as? KtBinaryExpression
            val isThisNullCheck = { checked: KtExpression?, other: KtExpression? ->
                checked is KtThisExpression && other != null && KtPsiUtil.isNullConstant(other)
            }
            return check != null &&
                check.operationToken in nullityOperations &&
                (isThisNullCheck(check.left, check.right) || isThisNullCheck(check.right, check.left))
        }
    }
}

internal fun KtExpression.verboseNullabilityTarget(): VerboseNullabilityAndEmptinessTarget? =
    verboseNullabilityChunks()?.let(::VerboseNullabilityAndEmptinessTarget)

private fun KtExpression.verboseNullabilityChunks(): List<KtExpression>? =
    when (val expression = KtPsiUtil.safeDeparenthesize(this)) {
        is KtNameReferenceExpression, is KtThisExpression -> listOf(expression)

        is KtDotQualifiedExpression -> (expression.selectorExpression as? KtNameReferenceExpression)
            ?.let { selector -> expression.receiverExpression.verboseNullabilityChunks()?.plus(selector) }

        else -> null
    }
