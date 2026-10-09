package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression

internal val DUPLICATE_ARGUMENTS_MAP_FUNCTIONS = setOf(
    "mapOf",
    "mutableMapOf",
    "hashMapOf",
    "linkedMapOf",
    "sortedMapOf",
)

internal val DUPLICATE_ARGUMENTS_SET_FUNCTIONS = setOf(
    "setOf",
    "mutableSetOf",
    "hashSetOf",
    "linkedSetOf",
    "sortedSetOf",
)

internal class DuplicateArgumentsInSetOfAndMapOfFunctionsKeys(private val call: KtCallExpression) {

    val duplicates: Map<Any?, List<KtExpression>>
        get() = analyze(call) {
            val callableId = call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
            val name = callableId?.run { callableName.asString() }
            val arguments = call.valueArguments.mapNotNull { it.getArgumentExpression() }
            val pairs = arguments.filterIsInstance<KtBinaryExpression>()
            val keyedArguments = when {
                callableId?.packageName != StandardClassIds.BASE_COLLECTIONS_PACKAGE -> emptyList()
                name in DUPLICATE_ARGUMENTS_MAP_FUNCTIONS -> pairs.map { pair -> pair.left to pair }
                name in DUPLICATE_ARGUMENTS_SET_FUNCTIONS -> arguments.map { element -> element to element }
                else -> emptyList()
            }
            keyedArguments
                .mapNotNull { (key, argument) -> key?.evaluate()?.let { constant -> constant.value to argument } }
                .groupBy({ it.first }, { it.second })
                .filterValues { it.size > 1 }
        }
}
