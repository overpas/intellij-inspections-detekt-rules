package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver

private val redundantAsSequenceName = Name.identifier("asSequence")

private val redundantAsSequenceFunctions = setOf(
    CallableId(StandardClassIds.BASE_SEQUENCES_PACKAGE, redundantAsSequenceName),
    CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, redundantAsSequenceName),
)

private val redundantAsSequenceTerminations = setOf(
    "all",
    "any",
    "asIterable",
    "asSequence",
    "associate",
    "associateBy",
    "associateByTo",
    "associateTo",
    "average",
    "contains",
    "count",
    "elementAt",
    "elementAtOrElse",
    "elementAtOrNull",
    "filterIndexedTo",
    "filterIsInstanceTo",
    "filterNotNullTo",
    "filterNotTo",
    "filterTo",
    "find",
    "findLast",
    "first",
    "firstNotNullOf",
    "firstNotNullOfOrNull",
    "firstOrNull",
    "flatMapTo",
    "flatMapIndexedTo",
    "fold",
    "foldIndexed",
    "groupBy",
    "groupByTo",
    "groupingBy",
    "indexOf",
    "indexOfFirst",
    "indexOfLast",
    "joinTo",
    "joinToString",
    "last",
    "lastIndexOf",
    "lastOrNull",
    "mapIndexedNotNullTo",
    "mapIndexedTo",
    "mapNotNullTo",
    "mapTo",
    "maxOrNull",
    "maxByOrNull",
    "maxWithOrNull",
    "maxOf",
    "maxOfOrNull",
    "maxOfWith",
    "maxOfWithOrNull",
    "minOrNull",
    "minByOrNull",
    "minWithOrNull",
    "minOf",
    "minOfOrNull",
    "minOfWith",
    "minOfWithOrNull",
    "none",
    "partition",
    "reduce",
    "reduceIndexed",
    "reduceIndexedOrNull",
    "reduceOrNull",
    "single",
    "singleOrNull",
    "sum",
    "sumBy",
    "sumByDouble",
    "sumOf",
    "toCollection",
    "toHashSet",
    "toList",
    "toMutableList",
    "toMutableSet",
    "toSet",
    "toSortedSet",
    "unzip",
)

private val redundantAsSequenceTransformations = setOf(
    "chunked",
    "distinct",
    "distinctBy",
    "drop",
    "dropWhile",
    "filter",
    "filterIndexed",
    "filterIsInstance",
    "filterNot",
    "filterNotNull",
    "flatMap",
    "flatMapIndexed",
    "flatten",
    "map",
    "mapIndexed",
    "mapIndexedNotNull",
    "mapNotNull",
    "minus",
    "minusElement",
    "onEach",
    "onEachIndexed",
    "plus",
    "plusElement",
    "requireNoNulls",
    "runningFold",
    "runningFoldIndexed",
    "runningReduce",
    "runningReduceIndexed",
    "scan",
    "scanIndexed",
    "sorted",
    "sortedBy",
    "sortedByDescending",
    "sortedDescending",
    "sortedWith",
    "take",
    "takeWhile",
    "windowed",
    "withIndex",
    "zipWithNext",
)

context(session: KaSession)
internal fun KtQualifiedExpression.asSequenceReceiverClassId(): ClassId? =
    with(session) {
        val symbol = selectorExpression.redundantAsSequenceCall()?.symbol
        val receiverType = symbol?.takeIf { it.callableId in redundantAsSequenceFunctions }?.receiverType
        receiverType?.expandedSymbol?.classId
    }

context(session: KaSession)
internal fun KtQualifiedExpression.isRedundantOnSequence(): Boolean =
    with(session) {
        val call = selectorExpression as? KtCallExpression
        val explicitType = selectorExpression
            .redundantAsSequenceCall()
            ?.run { typeArgumentsMapping.values.singleOrNull() }
        val receiverType = (receiverExpression.expressionType as? KaClassType)
            ?.takeIf { type -> type.classId == StandardClassIds.Sequence }
        val elementType = receiverType?.typeArguments?.singleOrNull()?.type
        val isSameType = explicitType != null && elementType != null && explicitType.semanticallyEquals(elementType)
        call?.typeArgumentList == null || isSameType
    }

context(session: KaSession)
internal fun KtQualifiedExpression.isRedundantOnIterable(): Boolean {
    val parent = getQualifiedExpressionForReceiver()
    val next = parent?.getQualifiedExpressionForReceiver()
    return parent.callsSequenceFunction(redundantAsSequenceTerminations) &&
        !next.callsSequenceFunction(redundantAsSequenceTerminations + redundantAsSequenceTransformations)
}

context(session: KaSession)
private fun KtExpression?.callsSequenceFunction(names: Set<String>): Boolean {
    val call = (this as? KtQualifiedExpression)?.selectorExpression
    val name = (call as? KtCallExpression)?.calleeExpression?.text.orEmpty()
    return name in names &&
        call.redundantAsSequenceCall()?.run { symbol.callableId } ==
        CallableId(StandardClassIds.BASE_SEQUENCES_PACKAGE, Name.identifier(name))
}

context(session: KaSession)
private fun KtExpression?.redundantAsSequenceCall(): KaFunctionCall<*>? =
    with(session) {
        (this@redundantAsSequenceCall as? KtCallExpression)?.resolveToCall()?.successfulFunctionCallOrNull()
    }
