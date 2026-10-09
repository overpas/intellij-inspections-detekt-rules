package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal fun UselessCallOnCollectionCall.mapNotNullMessage(): String? {
    val replacement = call.symbol.callableId?.let { it.callableName.asString().replace("NotNull", "") }
    val isUsingReplacementLabel = expression.collectDescendantsOfType<KtExpressionWithLabel>()
        .any { it.getLabelName() == replacement }
    val isReturningNotNull = call.valueArgumentMapping.keys.lastOrNull()
        ?.let { UselessCallOnCollectionArgument(session, it).isReturningNotNull } != false
    val isReducible = isElementTypeNotNull && isReturningNotNull && !isUsingReplacementLabel
    return "Call on collection type may be reduced".takeIf { isReducible }
}
