package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

private const val FIRST_OR_NULL_FIRST = "first"

private val FIRST_OR_NULL_FIRST_IDS = setOf(
    CallableId(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, Name.identifier(FIRST_OR_NULL_FIRST)),
    CallableId(StandardClassIds.BASE_TEXT_PACKAGE, Name.identifier(FIRST_OR_NULL_FIRST)),
)

internal class ReplaceIfExpressionWithFirstOrNullRead(expression: KtExpression) {

    private val access = KtPsiUtil.safeDeparenthesize(expression) as? KtArrayAccessExpression

    private val qualified = KtPsiUtil.safeDeparenthesize(expression) as? KtDotQualifiedExpression

    private val call = qualified?.selectorExpression as? KtCallExpression

    private val isZeroIndexAccess = access?.indexExpressions?.singleOrNull()?.firstOrNullIntegerConstant == 0

    private val isZeroGetCall = call?.calleeExpression?.text == "get" &&
        call.valueArguments.singleOrNull()?.getArgumentExpression()?.firstOrNullIntegerConstant == 0

    private val isFirstCall = call?.calleeExpression?.text == FIRST_OR_NULL_FIRST &&
        call.valueArguments.isEmpty() &&
        call.lambdaArguments.isEmpty()

    val receiver: KtExpression? = when {
        isZeroIndexAccess -> access?.arrayExpression
        isZeroGetCall || isFirstCall -> qualified?.receiverExpression
        else -> null
    }

    val calls: Map<KtCallExpression, Set<CallableId>> =
        call?.takeIf { isFirstCall }?.let { mapOf(it to FIRST_OR_NULL_FIRST_IDS) }.orEmpty()
}
