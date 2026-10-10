package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal class VerboseNullabilityAndEmptinessContent(
    val target: VerboseNullabilityAndEmptinessTarget,
    private val call: KtCallExpression,
    val function: VerboseNullabilityAndEmptinessFunction,
    val isPositive: Boolean,
) {

    context(session: KaSession)
    fun isApplicable(): Boolean =
        with(session) {
            val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
            val receiverType = (resolved?.dispatchReceiver ?: resolved?.extensionReceiver)?.type
            val symbol = resolved?.symbol
            receiverType != null &&
                !receiverType.isMarkedNullable &&
                (receiverType as? KaClassType)?.classId !in primitiveArrays &&
                symbol != null &&
                (symbol.allOverriddenSymbols + symbol).any { function.isDeclaredAs(it.callableId?.asSingleFqName()) }
        }

    private companion object {
        val primitiveArrays = StandardClassIds.primitiveArrayTypeByElementType.values +
            StandardClassIds.unsignedArrayTypeByElementType.values
    }
}

internal fun KtExpression.verboseNullabilityContent(): VerboseNullabilityAndEmptinessContent? {
    val negated = (this as? KtPrefixExpression)?.takeIf { it.operationToken == KtTokens.EXCL }?.baseExpression
    val check = negated?.let(KtPsiUtil::safeDeparenthesize) ?: this
    val call = ((check as? KtDotQualifiedExpression)?.selectorExpression ?: check) as? KtCallExpression
    val target = when (check) {
        is KtDotQualifiedExpression -> check.receiverExpression.verboseNullabilityTarget()
        is KtCallExpression -> VerboseNullabilityAndEmptinessTarget(listOf(check))
        else -> null
    }
    val function = VerboseNullabilityAndEmptinessFunction.entries.find {
        it.functionName == call?.calleeExpression?.text
    }
    return if (call != null && target != null && function != null) {
        VerboseNullabilityAndEmptinessContent(
            target = target,
            call = call,
            function = function,
            isPositive = function.isPositive xor (negated != null),
        )
    } else {
        null
    }
}
