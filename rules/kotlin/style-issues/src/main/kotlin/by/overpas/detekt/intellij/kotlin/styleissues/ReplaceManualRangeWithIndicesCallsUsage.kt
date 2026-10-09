package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtContainerNode
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal class ReplaceManualRangeWithIndicesCallsUsage(
    usage: KtNameReferenceExpression,
    explicitReceiver: KtExpression?,
) {

    private val arrayAccess = (usage.parent as? KtContainerNode)?.parent as? KtArrayAccessExpression

    private val argumentList = (usage.parent as? KtValueArgument)?.parent as? KtValueArgumentList

    private val getCall = (argumentList?.parent as? KtCallExpression)
        ?.takeIf { it.calleeExpression?.text == "get" && it.valueArguments.size == 1 }
        ?.parent as? KtDotQualifiedExpression

    private val access: KtExpression? = arrayAccess?.takeIf { it.indexExpressions.size == 1 } ?: getCall

    private val accessReceiver = arrayAccess?.arrayExpression ?: getCall?.receiverExpression

    private val isAssigned = (access?.parent as? KtBinaryExpression)
        ?.takeIf { KtPsiUtil.isAssignment(it) }
        ?.left == access

    private val isReceiverMatching = if (explicitReceiver == null) {
        accessReceiver?.let(KtPsiUtil::safeDeparenthesize) is KtThisExpression
    } else {
        accessReceiver?.manualRangeText == explicitReceiver.manualRangeText
    }

    val isElementAccess: Boolean = access != null && isReceiverMatching && !isAssigned
}

private val KtExpression.manualRangeText: String
    get() = KtPsiUtil.safeDeparenthesize(this).text.filterNot(Char::isWhitespace)
