package by.overpas.detekt.intellij.kotlin.styleissues

import com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private const val FIRST_OR_NULL_COUNT = "count"

private val FIRST_OR_NULL_SIZE_NAMES = setOf("size", "length")

private val FIRST_OR_NULL_COUNT_IDS = setOf(
    CallableId(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, Name.identifier(FIRST_OR_NULL_COUNT)),
    CallableId(StandardClassIds.BASE_TEXT_PACKAGE, Name.identifier(FIRST_OR_NULL_COUNT)),
)

private val FIRST_OR_NULL_RIGHT_BOUNDS: Map<IElementType, Pair<Int, Boolean>> = mapOf(
    KtTokens.EQEQ to (0 to true),
    KtTokens.EXCLEQ to (0 to false),
    KtTokens.GTEQ to (1 to false),
    KtTokens.GT to (0 to false),
    KtTokens.LTEQ to (0 to true),
    KtTokens.LT to (1 to true),
)

private val FIRST_OR_NULL_LEFT_BOUNDS: Map<IElementType, Pair<Int, Boolean>> = mapOf(
    KtTokens.EQEQ to (0 to true),
    KtTokens.EXCLEQ to (0 to false),
    KtTokens.GTEQ to (0 to true),
    KtTokens.GT to (1 to true),
    KtTokens.LTEQ to (1 to false),
    KtTokens.LT to (0 to false),
)

internal class ReplaceIfExpressionWithFirstOrNullSizeCheck(binary: KtBinaryExpression) {

    private val rightBound = FIRST_OR_NULL_RIGHT_BOUNDS[binary.operationToken]

    private val leftBound = FIRST_OR_NULL_LEFT_BOUNDS[binary.operationToken]

    private val operand: Pair<KtExpression?, Boolean>? = when {
        rightBound != null && binary.right?.firstOrNullIntegerConstant == rightBound.first ->
            binary.left to rightBound.second

        leftBound != null && binary.left?.firstOrNullIntegerConstant == leftBound.first ->
            binary.right to leftBound.second

        else -> null
    }

    private val sizeAccess = operand?.first?.let { KtPsiUtil.safeDeparenthesize(it) } as? KtDotQualifiedExpression

    private val sizeName = (sizeAccess?.selectorExpression as? KtNameReferenceExpression)?.getReferencedName()

    private val countCall = (sizeAccess?.selectorExpression as? KtCallExpression)?.takeIf { call ->
        call.calleeExpression?.text == FIRST_OR_NULL_COUNT &&
            call.valueArguments.isEmpty() &&
            call.lambdaArguments.isEmpty()
    }

    val receiver: KtExpression? =
        sizeAccess?.takeIf { sizeName in FIRST_OR_NULL_SIZE_NAMES || countCall != null }?.receiverExpression

    val isEmptyWhenTrue: Boolean? = operand?.second

    val calls: Map<KtCallExpression, Set<CallableId>> =
        countCall?.let { mapOf(it to FIRST_OR_NULL_COUNT_IDS) }.orEmpty()
}
