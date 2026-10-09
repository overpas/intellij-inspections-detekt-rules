package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtThrowExpression

private val NULL_POINTER_EXCEPTIONS = setOf(
    "kotlin.KotlinNullPointerException",
    "kotlin.NullPointerException",
    "java.lang.NullPointerException",
)

internal fun KtExpression.isIfThenNullOrBlock(): Boolean {
    val inner = ifThenSingleStatement() ?: this
    return inner is KtBlockExpression || inner.node.elementType == KtNodeTypes.NULL
}

internal fun KtExpression.isIfThenArgumentOf(call: KtExpression): Boolean {
    val arguments = (call as? KtCallExpression)
        ?.run { valueArguments.map { it.getArgumentExpression() } }
        .orEmpty()
    val isPassed = arguments.any { argument -> argument?.isIfThenSimplifiableTo(this) == true }
    return isPassed && arguments.all { it is KtNameReferenceExpression }
}

internal fun KtExpression.isIfThenFirstReceiverOf(qualified: KtExpression): Boolean =
    (qualified as? KtDotQualifiedExpression)
        ?.run { ifThenLeftMostReceiver().isIfThenSimplifiableTo(this@isIfThenFirstReceiverOf) } == true

context(session: KaSession)
internal fun KtExpression.isIfThenNpeWithoutArguments(): Boolean =
    with(session) {
        val thrown = (this@isIfThenNpeWithoutArguments as? KtThrowExpression)?.thrownExpression as? KtCallExpression
        val call = thrown
            ?.takeIf { it.calleeExpression is KtNameReferenceExpression && it.valueArguments.isEmpty() }
            ?.resolveToCall()
        val constructor = call?.successfulFunctionCallOrNull()?.symbol as? KaConstructorSymbol
        constructor?.containingClassId?.run { asSingleFqName().asString() } in NULL_POINTER_EXCEPTIONS
    }

context(session: KaSession)
internal fun KtExpression.hasNonNullIfThenReceivers(): Boolean =
    with(session) {
        generateSequence(this@hasNonNullIfThenReceivers) { (it as? KtDotQualifiedExpression)?.receiverExpression }
            .all { receiver ->
                receiver.expressionType?.isNullable == false ||
                    receiver.smartCastInfo?.run { smartCastType.isNullable } == false
            }
    }
