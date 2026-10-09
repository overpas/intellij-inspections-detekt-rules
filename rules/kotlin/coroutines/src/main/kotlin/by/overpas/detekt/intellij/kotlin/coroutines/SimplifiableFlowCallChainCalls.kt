package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

internal class SimplifiableFlowCallChainCalls(expression: KtQualifiedExpression) {

    private val second = expression.selectorExpression as? KtCallExpression

    private val first = expression.receiverExpression.let {
        ((it as? KtQualifiedExpression)?.selectorExpression ?: it) as? KtCallExpression
    }

    private val names = first?.calleeExpression?.text to second?.calleeExpression?.text

    val firstCallee: KtExpression? = first?.calleeExpression

    fun isCandidate(): Boolean =
        names in CHAINS &&
            first?.run { lambdaArguments.singleOrNull() }?.anyDescendantOfType<KtReturnExpression>() != true

    @OptIn(KaExperimentalApi::class)
    fun isSimplifiable(session: KaSession): Boolean =
        with(session) {
            val firstId = first?.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
            val secondCall = second?.resolveToCall()?.successfulFunctionCallOrNull()
            firstId == CallableId(FLOW_PACKAGE, Name.identifier(names.first.orEmpty())) &&
                secondCall?.run {
                    symbol.callableId == CallableId(FLOW_PACKAGE, Name.identifier(names.second.orEmpty())) &&
                        valueArgumentMapping.values.all { it.returnType.functionTypeKind == null }
                } == true
        }

    private companion object {
        val FLOW_PACKAGE = FqName("kotlinx.coroutines.flow")
        const val FILTER = "filter"
        val CHAINS = setOf(
            FILTER to "first",
            FILTER to "firstOrNull",
            FILTER to "count",
            "map" to "filterNotNull",
        )
    }
}
