package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

private val uselessCallOnCollectionPackages = setOf(FqName("kotlin.collections"), FqName("kotlin.sequences"))

private val uselessCallOnCollectionChecks: Map<String, UselessCallOnCollectionCall.() -> String?> = mapOf(
    "filterNotNull" to UselessCallOnCollectionCall::filterNotNullMessage,
    "filterIsInstance" to UselessCallOnCollectionCall::filterIsInstanceMessage,
    "filter" to UselessCallOnCollectionCall::constantFilterMessage,
    "mapNotNull" to UselessCallOnCollectionCall::mapNotNullMessage,
    "mapNotNullTo" to UselessCallOnCollectionCall::mapNotNullMessage,
    "mapIndexedNotNull" to UselessCallOnCollectionCall::mapNotNullMessage,
    "mapIndexedNotNullTo" to UselessCallOnCollectionCall::mapNotNullMessage,
)

internal fun KtQualifiedExpression.uselessCallOnCollectionMessage(): String? {
    val qualified = this
    val selector = selectorExpression as? KtCallExpression
    val calleeName = selector?.calleeExpression?.text
    val messageOf = uselessCallOnCollectionChecks[calleeName]
    return if (selector == null || messageOf == null) {
        null
    } else {
        analyze(selector) {
            val call = selector.resolveToCall()?.successfulFunctionCallOrNull()
            val callableId = call?.let { it.symbol.callableId }
            val isTarget = callableId?.let {
                it.packageName in uselessCallOnCollectionPackages && it.callableName.asString() == calleeName
            } == true
            call?.takeIf { isTarget }?.let { UselessCallOnCollectionCall(this, qualified, it).messageOf() }
        }
    }
}
