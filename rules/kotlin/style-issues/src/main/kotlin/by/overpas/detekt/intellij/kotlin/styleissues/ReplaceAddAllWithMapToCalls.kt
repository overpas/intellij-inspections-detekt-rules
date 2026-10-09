package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression

private val addAllNames = setOf("addAll", "plusAssign")

private val operations = mapOf(
    CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier("map")) to "map",
    CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier("filter")) to "filter",
)

internal fun KtCallExpression.replaceAddAllWithMapToCallee(): KtNameReferenceExpression? =
    (calleeExpression as? KtNameReferenceExpression)
        ?.takeIf { parent !is KtSafeQualifiedExpression && it.getReferencedName() in addAllNames }

context(session: KaSession)
internal fun KtBinaryExpression.replaceAddAllWithMapToOp(): String? =
    resolvedCall()
        ?.takeIf { ReplaceAddAllWithMapToSymbols.isPlusAssign(it.symbol) }
        ?.let { right?.resolvedOperation() }

context(session: KaSession)
internal fun KtCallExpression.replaceAddAllWithMapToOp(): String? =
    resolvedCall()
        ?.takeIf { ReplaceAddAllWithMapToSymbols.isAddAll(it) }
        ?.let { valueArguments.singleOrNull()?.getArgumentExpression()?.resolvedOperation() }

context(session: KaSession)
private fun KtElement.resolvedCall(): KaFunctionCall<*>? =
    with(session) { resolveToCall()?.successfulFunctionCallOrNull() }

context(session: KaSession)
private fun KtExpression.resolvedOperation(): String? {
    val call = (this as? KtQualifiedExpression)?.selectorExpression ?: this
    return operations[call.resolvedCall()?.run { symbol.callableId }]
}
