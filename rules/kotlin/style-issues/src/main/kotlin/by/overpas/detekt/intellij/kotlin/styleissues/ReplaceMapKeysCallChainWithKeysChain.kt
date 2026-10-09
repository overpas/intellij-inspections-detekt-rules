package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val REPLACE_MAP_KEYS_MAP_FQ_NAME = FqName("kotlin.collections.map")

private val REPLACE_MAP_KEYS_TO_SET_FQ_NAME = FqName("kotlin.collections.toSet")

internal class ReplaceMapKeysChain(
    private val chain: KtQualifiedExpression,
    private val mapCall: KtCallExpression,
    private val lambda: KtLambdaExpression,
) {

    val mapCallee: KtExpression
        get() = mapCall.calleeExpression ?: mapCall

    context(session: KaSession)
    fun isReplaceable(): Boolean =
        with(session) {
            val map = mapCall.resolveToCall()?.successfulFunctionCallOrNull()
            val toSet = chain.resolveToCall()?.successfulFunctionCallOrNull()
            map?.symbol?.callableId?.asSingleFqName() == REPLACE_MAP_KEYS_MAP_FQ_NAME &&
                map.extensionReceiver?.run { type.isSubtypeOf(StandardClassIds.Map) } == true &&
                toSet?.symbol?.callableId?.asSingleFqName() == REPLACE_MAP_KEYS_TO_SET_FQ_NAME &&
                lambda.mapsMapEntryToKey()
        }
}

internal fun KtQualifiedExpression.replaceMapKeysChain(): ReplaceMapKeysChain? {
    val first = receiverExpression.let { (it as? KtQualifiedExpression)?.selectorExpression ?: it }
    val mapCall = (first as? KtCallExpression)?.takeIf { it.calleeExpression?.text == "map" }
    val toSetCall = (selectorExpression as? KtCallExpression)?.takeIf { it.calleeExpression?.text == "toSet" }
    val argument = mapCall?.run { valueArguments.singleOrNull() }
    val lambda = ((argument as? KtLambdaArgument)?.getLambdaExpression() ?: argument?.getArgumentExpression())
        .let { it as? KtLambdaExpression }
        ?.takeIf { PsiTreeUtil.findChildOfType(it, PsiComment::class.java) == null }
    return if (mapCall != null && toSetCall != null && lambda != null) {
        ReplaceMapKeysChain(this, mapCall, lambda)
    } else {
        null
    }
}
