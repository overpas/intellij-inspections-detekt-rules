package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

internal class SimplifiableCallChainCandidate(expression: KtQualifiedExpression) {

    private val secondCall = expression.selectorExpression as? KtCallExpression

    private val firstCall = expression.receiverExpression.let { receiver ->
        (receiver as? KtQualifiedExpression)?.selectorExpression ?: receiver
    } as? KtCallExpression

    private val conversions = SIMPLIFIABLE_CALL_CHAIN_CONVERSIONS.filter { conversion ->
        conversion.firstName == firstCall?.calleeExpression?.text &&
            conversion.secondName == secondCall?.calleeExpression?.text
    }

    val firstCallee: KtExpression? = firstCall?.calleeExpression

    fun isCandidate(): Boolean =
        conversions.isNotEmpty() &&
            firstCall?.lambdaArguments?.singleOrNull()?.anyDescendantOfType<KtReturnExpression>() != true

    context(session: KaSession)
    fun isSimplifiable(): Boolean =
        with(session) {
            val first = firstCall?.resolveToCall()?.successfulFunctionCallOrNull()
            val second = secondCall?.resolveToCall()?.successfulFunctionCallOrNull()
            first != null &&
                second != null &&
                second.valueArgumentMapping.values.none { it.returnType is KaFunctionType } &&
                first.extensionReceiver?.run { type.isSubtypeOf(StandardClassIds.Map) } != true &&
                conversions.any { (firstId, secondId, kind) ->
                    firstId == first.symbol.callableId?.run { asSingleFqName().asString() } &&
                        secondId == second.symbol.callableId?.run { asSingleFqName().asString() } &&
                        SimplifiableCallChainFirstCall(first).isApplicable(kind)
                }
        }
}
