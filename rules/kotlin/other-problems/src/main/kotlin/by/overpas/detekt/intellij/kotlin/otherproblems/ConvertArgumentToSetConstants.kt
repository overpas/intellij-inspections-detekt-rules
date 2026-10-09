package by.overpas.detekt.intellij.kotlin.otherproblems

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private const val MAX_CONSTANT_COLLECTION_SIZE = 10

private val collectionFactoryFunctions = setOf(
    "kotlin.arrayOf",
    "kotlin.emptyArray",
    "kotlin.sequences.sequenceOf",
    "kotlin.sequences.emptySequence",
    "kotlin.collections.listOf",
    "kotlin.collections.emptyList",
)

context(session: KaSession)
internal fun KtExpression.isConstantCollectionForSet(): Boolean {
    val calls = if (this is KtNameReferenceExpression) valInitializerCalls() else listOfNotNull(setArgumentCallOrNull())
    return calls.isNotEmpty() && calls.all { it.isConstantCollectionCall() }
}

context(session: KaSession)
private fun KtCallExpression.isConstantCollectionCall(): Boolean =
    with(session) {
        val fqName = resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId?.asSingleFqName() }
        fqName?.asString() in collectionFactoryFunctions &&
            valueArguments.size <= MAX_CONSTANT_COLLECTION_SIZE &&
            valueArguments.all { it.getArgumentExpression()?.evaluate() != null }
    }
