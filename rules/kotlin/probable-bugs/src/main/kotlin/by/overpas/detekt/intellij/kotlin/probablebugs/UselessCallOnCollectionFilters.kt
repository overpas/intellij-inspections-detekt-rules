package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.typeParameters
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaTypeArgumentWithVariance
import org.jetbrains.kotlin.types.Variance

internal fun UselessCallOnCollectionCall.filterNotNullMessage(): String? =
    USELESS_CALL_ON_COLLECTION_REDUNDANT.takeIf { isElementTypeNotNull }

@OptIn(KaExperimentalApi::class)
internal fun UselessCallOnCollectionCall.filterIsInstanceMessage(): String? {
    val argument = receiverTypeArgument
    val isInVariance = argument is KaTypeArgumentWithVariance && argument.variance == Variance.IN_VARIANCE
    val element = elementType?.takeUnless { isInVariance || it is KaFlexibleType }
    val target = call.symbol.typeParameters.singleOrNull()?.let { call.typeArgumentsMapping[it] }
    val isRedundant = element != null && target != null && with(session) { element.isSubtypeOf(target) }
    return USELESS_CALL_ON_COLLECTION_REDUNDANT.takeIf { isRedundant }
}

internal fun UselessCallOnCollectionCall.constantFilterMessage(): String? {
    val filter = lambda?.let { UselessCallOnCollectionFilterLambda(session, it) }
    return when {
        filter?.isTrueConstant == true -> USELESS_CALL_ON_COLLECTION_REDUNDANT
        filter?.isFalseConstant == true -> "'.filter { false }' will return an empty collection."
        else -> null
    }
}
