package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

private const val FIRST_OR_NULL_IS_EMPTY = "isEmpty"

private const val FIRST_OR_NULL_IS_NOT_EMPTY = "isNotEmpty"

private val FIRST_OR_NULL_EMPTINESS_IDS = listOf(true, false).associateWith { isEmpty ->
    val name = Name.identifier(if (isEmpty) FIRST_OR_NULL_IS_EMPTY else FIRST_OR_NULL_IS_NOT_EMPTY)
    setOf(
        CallableId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, name),
        CallableId(StandardClassIds.BASE_TEXT_PACKAGE, name),
    )
}

internal class ReplaceIfExpressionWithFirstOrNullCondition(condition: KtExpression) {

    private val expression = KtPsiUtil.safeDeparenthesize(condition)

    private val qualified = expression as? KtDotQualifiedExpression

    private val callee = (qualified?.selectorExpression ?: expression) as? KtCallExpression

    private val emptinessCall = callee?.takeIf { call ->
        call.calleeExpression?.text in setOf(FIRST_OR_NULL_IS_EMPTY, FIRST_OR_NULL_IS_NOT_EMPTY) &&
            call.valueArguments.isEmpty() &&
            call.lambdaArguments.isEmpty()
    }

    private val sizeCheck = (expression as? KtBinaryExpression)?.let { ReplaceIfExpressionWithFirstOrNullSizeCheck(it) }

    val isEmptyWhenTrue: Boolean? =
        emptinessCall?.let { it.calleeExpression?.text == FIRST_OR_NULL_IS_EMPTY } ?: sizeCheck?.isEmptyWhenTrue

    val receiver: KtExpression? = when {
        emptinessCall == null -> sizeCheck?.receiver
        qualified != null -> qualified.receiverExpression
        else -> emptinessCall.firstOrNullScopeFunctionReceiver
    }

    val calls: Map<KtCallExpression, Set<CallableId>> =
        emptinessCall?.let { mapOf(it to FIRST_OR_NULL_EMPTINESS_IDS.getValue(isEmptyWhenTrue == true)) }
            ?: sizeCheck?.calls.orEmpty()
}

internal val KtExpression.firstOrNullIntegerConstant: Int?
    get() = (KtPsiUtil.deparenthesize(this) as? KtConstantExpression)
        ?.takeIf { it.node.elementType == KtNodeTypes.INTEGER_CONSTANT }
        ?.run { text.toIntOrNull() }

private val KtCallExpression.firstOrNullScopeFunctionReceiver: KtExpression?
    get() = getStrictParentOfType<KtLambdaExpression>()
        ?.parent
        ?.let { if (it is KtLambdaArgument) it.parent else (it as? KtValueArgument)?.parent?.parent }
        .let { it as? KtCallExpression }
        ?.let { scopeCall ->
            when (scopeCall.calleeExpression?.text) {
                "with" -> scopeCall.valueArguments.firstOrNull()?.getArgumentExpression()
                "run", "apply" -> (scopeCall.parent as? KtDotQualifiedExpression)?.receiverExpression
                else -> null
            }
        }
