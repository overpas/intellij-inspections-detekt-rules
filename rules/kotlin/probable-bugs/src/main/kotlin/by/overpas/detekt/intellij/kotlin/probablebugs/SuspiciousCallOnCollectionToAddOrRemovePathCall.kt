package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

internal class SuspiciousCallOnCollectionToAddOrRemovePathCall(private val call: KtExpression) {

    private val receiver: KtExpression? =
        when (call) {
            is KtBinaryExpression -> call.left
            is KtCallExpression -> call.getQualifiedExpressionForSelector()?.receiverExpression
            else -> null
        }

    private val argument: KtExpression? =
        when (call) {
            is KtBinaryExpression -> call.right
            is KtCallExpression -> call.valueArguments.singleOrNull()?.getArgumentExpression()
            else -> null
        }

    private val suspiciousCallableIds = listOf(
        StandardClassIds.BASE_COLLECTIONS_PACKAGE,
        StandardClassIds.BASE_SEQUENCES_PACKAGE,
    ).flatMap { packageName ->
        listOf("plus", "minus").map { CallableId(packageName, Name.identifier(it)) }
    }

    fun isSuspicious(): Boolean {
        val operands = listOfNotNull(receiver, argument)
        return operands.size == 2 &&
            operands.none { it is KtConstantExpression || it is KtStringTemplateExpression } &&
            analyze(call) {
                val argumentType = argument?.expressionType
                val argumentClassId = argumentType?.suspiciousPathClassId
                val elementClassId = argumentType?.suspiciousPathElementClassId()
                val callableId = call.resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId }
                argumentClassId != null &&
                    elementClassId != null &&
                    !argumentType.isSuspiciousPathContainer() &&
                    (
                        elementClassId == argumentClassId ||
                            elementClassId == receiver?.expressionType?.suspiciousPathElementClassId()
                        ) &&
                    callableId in suspiciousCallableIds
            }
    }

    fun message(isPlus: Boolean): String =
        if (isPlus) {
            "'plus' call iterates over the argument instead of adding it as a single element"
        } else {
            "'minus' call iterates over the argument instead of removing it as a single element"
        }
}
