package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaVariableAccessCall
import org.jetbrains.kotlin.analysis.api.resolution.singleCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtContainerNode
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private const val MANUAL_RANGE_UNTIL = "until"

private const val MANUAL_RANGE_RANGE_TO = "rangeTo"

private const val MANUAL_RANGE_RANGE_UNTIL = "rangeUntil"

private const val MANUAL_RANGE_INDICES_MESSAGE = "Range could be replaced with '.indices' call"

private const val MANUAL_RANGE_WITH_INDEX_MESSAGE = "Range can be replaced with withIndex()"

private const val MANUAL_RANGE_ELEMENTS_MESSAGE = "For loop over indices could be replaced with loop over elements"

private val MANUAL_RANGE_PRIMITIVES = setOf(
    StandardClassIds.Int,
    StandardClassIds.Long,
    StandardClassIds.Short,
    StandardClassIds.Byte,
    StandardClassIds.Char,
    StandardClassIds.UInt,
    StandardClassIds.ULong,
    StandardClassIds.UShort,
    StandardClassIds.UByte,
)

internal class ReplaceManualRangeWithIndicesCallsRange(private val expression: KtExpression) {

    private val binary = expression as? KtBinaryExpression

    private val qualified = expression as? KtDotQualifiedExpression

    private val qualifiedCall = qualified?.selectorExpression as? KtCallExpression

    private val name = binary?.run { operationReference.getReferencedName() }
        ?: qualifiedCall?.calleeExpression?.text

    private val isInclusive = name == ".." || (qualified != null && name == MANUAL_RANGE_RANGE_TO)

    private val isExclusive = name == "..<" ||
        (binary != null && name == MANUAL_RANGE_UNTIL) ||
        (qualified != null && name == MANUAL_RANGE_RANGE_UNTIL)

    private val left = binary?.left ?: qualified?.receiverExpression

    private val right = binary?.right ?: qualifiedCall?.valueArguments?.singleOrNull()?.getArgumentExpression()

    private val inclusiveTarget = (right as? KtBinaryExpression)
        ?.takeIf { it.operationToken == KtTokens.MINUS && it.right?.manualRangeIntConstant == 1 }
        ?.left
        ?: (right as? KtDotQualifiedExpression)?.takeIf { it.selectorExpression?.text == "lastIndex" }

    private val target = when {
        left?.manualRangeIntConstant != 0 -> null
        isExclusive -> right
        isInclusive -> inclusiveTarget
        else -> null
    }

    private val selectorName = ((target as? KtDotQualifiedExpression)?.selectorExpression ?: target)?.text

    private val explicitReceiver = (target as? KtQualifiedExpression)?.receiverExpression

    private val loop = (expression.parent as? KtContainerNode)?.parent as? KtForExpression

    private val parameter = loop?.loopParameter?.takeIf { loop.loopRange == expression }

    private val parameterReferences = loop?.body
        ?.collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedName() == parameter?.name }
        .orEmpty()

    fun message(): String? =
        target?.let { bound ->
            analyze(expression) {
                val callableId = expression.resolveToCall()?.singleFunctionCallOrNull()?.run { symbol.callableId }
                val variableCall = bound.resolveToCall()?.singleCallOrNull<KaVariableAccessCall>()
                val receiverType = variableCall?.let { it.dispatchReceiver?.type ?: it.extensionReceiver?.type }
                val receiver = receiverType?.let { ReplaceManualRangeWithIndicesCallsReceiver(this, it) }
                val usages = parameterReferences
                    .filter { reference ->
                        reference.references.filterIsInstance<KtReference>()
                            .firstNotNullOfOrNull { it.resolveToSymbol() }
                            ?.psi == parameter
                    }
                    .map { ReplaceManualRangeWithIndicesCallsUsage(it, explicitReceiver).isElementAccess }
                val message = when {
                    true !in usages -> MANUAL_RANGE_INDICES_MESSAGE.takeIf { receiver?.hasIndices == true }
                    false in usages -> MANUAL_RANGE_WITH_INDEX_MESSAGE
                    else -> MANUAL_RANGE_ELEMENTS_MESSAGE
                }
                val isApplicable = callableId?.isManualRangeStdlibFunction == true &&
                    receiver?.isBound(selectorName, isInclusive) == true
                message?.takeIf { isApplicable }
            }
        }
}

private val CallableId.isManualRangeStdlibFunction: Boolean
    get() = when (callableName.asString()) {
        MANUAL_RANGE_UNTIL -> packageName == StandardClassIds.BASE_RANGES_PACKAGE

        MANUAL_RANGE_RANGE_TO, MANUAL_RANGE_RANGE_UNTIL ->
            packageName == StandardClassIds.BASE_RANGES_PACKAGE || classId in MANUAL_RANGE_PRIMITIVES

        else -> false
    }

private val KtExpression.manualRangeIntConstant: Int?
    get() = (this as? KtConstantExpression)?.run { text.toIntOrNull() }
