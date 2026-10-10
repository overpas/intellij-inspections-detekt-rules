package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

private const val IT = "it"

internal fun KtLambdaExpression.hasRemovableArrowParameters(): Boolean {
    val parameters = functionLiteral.valueParameters
    val singleParameter = parameters.singleOrNull()
    val hasOnlyImplicitParameter = parameters.isEmpty() ||
        (singleParameter?.typeReference == null && singleParameter?.name == IT)
    return hasOnlyImplicitParameter &&
        getStrictParentOfType<KtWhenEntry>()?.expression != this &&
        getStrictParentOfType<KtContainerNodeForControlStructureBody>()?.expression != this
}

@OptIn(KaExperimentalApi::class)
internal fun KtLambdaExpression.hasOnlyOwnItReferences(): Boolean =
    analyze(this) {
        val literal = functionLiteral
        val literalSymbol = literal.symbol
        val hasExpectedType = literal.valueParameters.isEmpty() || expectedType != null
        hasExpectedType &&
            !literal.anyDescendantOfType<KtNameReferenceExpression> {
                it.text == IT && it.resolveSymbol()?.containingDeclaration != literalSymbol
            }
    }

internal fun KtLambdaExpression.keepsResolvedCallsWithoutArrow(): Boolean {
    val call = topLevelCall() ?: return true
    val qualified = call.parent as? KtQualifiedExpression
    val fullExpression = if (qualified?.selectorExpression == call) qualified else call
    val fragment = textWithoutArrow(fullExpression)?.let {
        KtPsiFactory(project).createExpressionCodeFragment(it, call).getContentElement()
    }
    return fragment != null && fragment.resolvedCalls() == fullExpression.resolvedCalls()
}

private fun KtLambdaExpression.topLevelCall(): KtCallExpression? =
    generateSequence(getStrictParentOfType<KtValueArgument>()?.getStrictParentOfType<KtCallExpression>()) {
        it.getStrictParentOfType<KtValueArgument>()?.getStrictParentOfType<KtCallExpression>()
    }.lastOrNull()

private fun KtLambdaExpression.textWithoutArrow(fullExpression: KtExpression): String? =
    functionLiteral.bodyExpression?.let { body ->
        val text = fullExpression.text
        val offset = textOffset - fullExpression.textOffset
        text.take(offset) + "{" + text.substring(offset + body.startOffsetInParent)
    }

private fun KtElement.resolvedCalls(): List<Any?> =
    analyze(this) {
        collectDescendantsOfType<KtCallExpression>().map { call ->
            val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
            symbol?.psi ?: symbol?.callableId
        }
    }
