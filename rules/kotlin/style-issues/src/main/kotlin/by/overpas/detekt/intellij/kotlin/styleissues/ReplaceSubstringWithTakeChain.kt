package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

context(session: KaSession)
internal fun KtDotQualifiedExpression.isReplaceSubstringChainSafe(): Boolean {
    val outer = parent as? KtDotQualifiedExpression
    val nextCall = outer?.selectorExpression?.takeIf { outer.receiverExpression == this } as? KtCallExpression
    return with(session) {
        val receiverType = receiverExpression.expressionType
        val nextReceiverType = nextCall?.resolveToCall()
            ?.successfulFunctionCallOrNull()
            ?.symbol
            ?.receiverParameter
            ?.returnType
        receiverType == null || nextReceiverType == null || receiverType.isSubtypeOf(nextReceiverType)
    }
}
