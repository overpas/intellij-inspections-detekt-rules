package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.analysis.api.types.KaTypeArgumentWithVariance
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.types.Variance

internal class UselessCallOnFlowCall(
    private val session: KaSession,
    receiver: KtExpression,
    call: KtCallExpression,
) {

    private val function = with(session) {
        call.resolveToCall()
            ?.successfulFunctionCallOrNull()
            ?.takeIf { it.symbol.callableId?.packageName == FLOW_PACKAGE }
    }

    private val elementArgument = with(session) {
        (receiver.expressionType as? KaClassType)?.run { typeArguments.singleOrNull() }
    }

    fun isRedundantNullFilter(): Boolean =
        with(session) { function != null && elementArgument?.type?.isNullable == false }

    fun isRedundantInstanceFilter(): Boolean =
        with(session) {
            val elementType = elementArgument?.type
            val filteredType = function?.run { typeArgumentsMapping.values.singleOrNull() }
            elementType != null &&
                filteredType != null &&
                elementType !is KaFlexibleType &&
                (elementArgument as? KaTypeArgumentWithVariance)?.variance != Variance.IN_VARIANCE &&
                elementType.isSubtypeOf(filteredType)
        }

    fun isReducibleMapNotNull(): Boolean =
        isRedundantNullFilter() &&
            function?.valueArgumentMapping?.keys?.lastOrNull()?.let { returnsNotNull(it) } != false

    @OptIn(KaExperimentalApi::class)
    private fun returnsNotNull(transform: KtExpression): Boolean =
        with(session) {
            val lambda = ((transform as? KtLabeledExpression)?.baseExpression ?: transform) as? KtLambdaExpression
            val functionSymbol = lambda?.run { functionLiteral.symbol }
            (transform.expressionType as? KaFunctionType)?.run { returnType.isNullable } == false ||
                (
                    lambda?.bodyExpression?.expressionType?.isNullable == false &&
                        lambda.collectDescendantsOfType<KtReturnExpression>().none { returned ->
                            returned.resolveSymbol() == functionSymbol &&
                                returned.returnedExpression?.expressionType?.isNullable == true
                        }
                    )
        }

    private companion object {
        val FLOW_PACKAGE = FqName("kotlinx.coroutines.flow")
    }
}
