package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

private val SIMPLIFIABLE_CALL_CHAIN_SUM_TYPES =
    setOf(
        StandardClassIds.Int,
        StandardClassIds.UInt,
        StandardClassIds.Long,
        StandardClassIds.ULong,
        StandardClassIds.Double,
    )

internal class SimplifiableCallChainFirstCall(private val call: KaFunctionCall<*>) {

    private val lastArgument = call.valueArgumentMapping.entries.lastOrNull()

    private val lambdaReturnType = (lastArgument?.run { value.returnType } as? KaFunctionType)?.returnType

    private val lambdaResult = (lastArgument?.key as? KtLambdaExpression)?.bodyExpression?.lastBlockStatementOrThis()

    context(session: KaSession)
    fun isApplicable(kind: SimplifiableCallChainKind): Boolean =
        with(session) {
            val lambdaClassId = (lambdaReturnType as? KaClassType)?.classId
            val isIntLiteral =
                lambdaClassId == StandardClassIds.Int && SimplifiableCallChainLiteral(lambdaResult).isLiteral()
            val isSummable = lambdaClassId in SIMPLIFIABLE_CALL_CHAIN_SUM_TYPES && !isIntLiteral
            when (kind) {
                SimplifiableCallChainKind.PLAIN -> true

                SimplifiableCallChainKind.MAP_NOT_NULL ->
                    (call.extensionReceiver?.type as? KaClassType)?.classId !in
                        StandardClassIds.elementTypeByPrimitiveArrayType.keys

                SimplifiableCallChainKind.JOIN ->
                    lambdaReturnType?.isSubtypeOf(StandardClassIds.CharSequence) != false &&
                        call.valueArgumentMapping.keys.none { argument ->
                            argument.anyDescendantOfType<KtCallExpression> { nested ->
                                val symbol = nested.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
                                (symbol as? KaNamedFunctionSymbol)?.isSuspend == true
                            }
                        }

                SimplifiableCallChainKind.BY -> lambdaResult?.expressionType?.isMarkedNullable != true

                SimplifiableCallChainKind.SUM_OF -> lambdaReturnType == null || isSummable
            }
        }
}
