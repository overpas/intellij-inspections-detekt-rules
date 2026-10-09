package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

internal val FILTER_IS_INSTANCE_NAMES = setOf("filterIsInstance", "filterIsInstanceTo")

private val FILTER_IS_INSTANCE_CALLABLE_IDS =
    listOf(StandardClassIds.BASE_COLLECTIONS_PACKAGE, StandardClassIds.BASE_SEQUENCES_PACKAGE).flatMap { packageName ->
        FILTER_IS_INSTANCE_NAMES.map { CallableId(packageName, Name.identifier(it)) }
    }

internal fun KtCallExpression.isAlwaysEmptyFilterIsInstance(): Boolean =
    analyze(this) {
        resolveToCall()?.successfulFunctionCallOrNull()?.run {
            val elementType = extensionReceiver?.run { type.singleTypeArgument() }
            val targetType = signature.returnType.singleTypeArgument()
            symbol.callableId in FILTER_IS_INSTANCE_CALLABLE_IDS &&
                elementType != null &&
                targetType != null &&
                FilterIsInstanceResultIsAlwaysEmptyType(targetType)
                    .isDisjointFrom(FilterIsInstanceResultIsAlwaysEmptyType(elementType))
        } == true
    }

private fun KaType.singleTypeArgument(): KaType? =
    (this as? KaClassType)?.typeArguments?.singleOrNull()?.type
