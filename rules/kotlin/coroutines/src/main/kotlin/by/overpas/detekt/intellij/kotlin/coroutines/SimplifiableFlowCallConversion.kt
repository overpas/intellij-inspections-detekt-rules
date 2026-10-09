package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

private val FLOW_PACKAGE = FqName("kotlinx.coroutines.flow")

private val FLAT_MAP_MERGE_ID = CallableId(FLOW_PACKAGE, Name.identifier("flatMapMerge"))

private val FLAT_MAP_CONCAT_ID = CallableId(FLOW_PACKAGE, Name.identifier("flatMapConcat"))

private val FILTER_ID = CallableId(FLOW_PACKAGE, Name.identifier("filter"))

internal class SimplifiableFlowCallConversion(
    private val session: KaSession,
    private val expression: KtCallExpression,
) {

    fun replacement(): String? {
        val call = with(session) { expression.resolveToCall()?.successfulFunctionCallOrNull() } ?: return null
        val arguments = call.valueArgumentMapping.entries.associateBy({ it.value.name.asString() }, { it.key })
        val transform = SimplifiableFlowCallLambda(arguments["transform"] as? KtLambdaExpression)
        val singleLambda = expression.valueArguments.singleOrNull()?.getArgumentExpression() as? KtLambdaExpression
        return when (call.symbol.callableId) {
            FLAT_MAP_MERGE_ID ->
                "flattenMerge(${arguments["concurrency"]?.parent?.text.orEmpty()})".takeIf { transform.isIdentity() }

            FLAT_MAP_CONCAT_ID -> "flattenConcat()".takeIf { transform.isIdentity() }

            FILTER_ID -> SimplifiableFlowCallFilter(
                session,
                call,
                SimplifiableFlowCallLambda(singleLambda),
            ).replacement()

            else -> null
        }
    }
}
