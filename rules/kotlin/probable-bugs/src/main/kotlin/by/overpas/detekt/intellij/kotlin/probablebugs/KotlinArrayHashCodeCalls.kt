package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal fun KtQualifiedExpression.noArgumentCallNamed(name: Name): KtCallExpression? =
    (selectorExpression as? KtCallExpression)
        ?.takeIf { it.valueArguments.isEmpty() && it.calleeExpression?.text == name.asString() }

context(session: KaSession)
internal fun KtQualifiedExpression.isArrayMemberCall(callableId: CallableId): Boolean =
    with(session) {
        val function = selectorExpression?.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
        receiverExpression.expressionType?.isArrayOrPrimitiveArray == true && function?.callableId == callableId
    }
