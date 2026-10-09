package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtThisExpression

private val FIRST_OR_NULL_SUPERTYPES = listOf(
    StandardClassIds.List,
    StandardClassIds.CharSequence,
    StandardClassIds.Set,
)

private val FIRST_OR_NULL_ARRAYS = buildSet {
    add(StandardClassIds.Array)
    addAll(StandardClassIds.primitiveArrayTypeByElementType.values)
    addAll(StandardClassIds.unsignedArrayTypeByElementType.values)
}

internal class ReplaceIfExpressionWithFirstOrNullCandidate(private val expression: KtIfExpression) {

    private val condition = expression.condition?.let(::ReplaceIfExpressionWithFirstOrNullCondition)

    private val isEmptyWhenTrue = condition?.isEmptyWhenTrue

    private val nullBranch = if (isEmptyWhenTrue == true) expression.then else expression.`else`

    private val valueBranch = if (isEmptyWhenTrue == true) expression.`else` else expression.then

    private val isNullBranch = nullBranch?.firstOrNullSingleStatement?.node?.elementType == KtNodeTypes.NULL

    private val read = valueBranch?.firstOrNullSingleStatement
        ?.takeIf { isEmptyWhenTrue != null && isNullBranch }
        ?.let(::ReplaceIfExpressionWithFirstOrNullRead)

    private val conditionReceiver = condition?.receiver?.takeIf { it.isFirstOrNullStableReceiver }

    private val receiver = read?.receiver?.takeIf { it.firstOrNullText == conditionReceiver?.firstOrNullText }

    private val calls = condition?.calls.orEmpty() + read?.calls.orEmpty()

    fun isReplaceable(): Boolean =
        receiver?.let { readReceiver ->
            analyze(expression) {
                val type = readReceiver.expressionType
                val isSupported = type != null &&
                    (
                        FIRST_OR_NULL_SUPERTYPES.any { type.isSubtypeOf(it) } ||
                            (type as? KaClassType)?.classId in FIRST_OR_NULL_ARRAYS
                        )
                isSupported &&
                    calls.all { (call, ids) ->
                        call.resolveToCall()?.singleFunctionCallOrNull()?.run { symbol.callableId } in ids
                    }
            }
        } == true
}

private val KtExpression.firstOrNullSingleStatement: KtExpression?
    get() {
        val inner = KtPsiUtil.safeDeparenthesize(this, true)
        return if (inner is KtBlockExpression) {
            inner.statements.singleOrNull()?.let { statement ->
                KtPsiUtil.safeDeparenthesize(statement, true).takeUnless { it is KtLambdaExpression }
            }
        } else {
            inner
        }
    }

private val KtExpression.isFirstOrNullStableReceiver: Boolean
    get() = generateSequence(KtPsiUtil.safeDeparenthesize(this)) { part ->
        (part as? KtDotQualifiedExpression)?.let { KtPsiUtil.safeDeparenthesize(it.receiverExpression) }
    }.all { part ->
        part is KtNameReferenceExpression ||
            part is KtThisExpression ||
            (part as? KtDotQualifiedExpression)?.selectorExpression is KtNameReferenceExpression
    }

private val KtExpression.firstOrNullText: String
    get() = KtPsiUtil.safeDeparenthesize(this).text.filterNot(Char::isWhitespace)
