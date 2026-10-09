package by.overpas.detekt.intellij.kotlin.redundantconstructs

import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSamConstructorSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getStartOffsetIn

internal fun KtCallExpression.hasBlockingLabeledReturn(): Boolean {
    val lambda = samLambdaArgument()
    val name = (calleeExpression as? KtSimpleNameExpression)?.getReferencedNameAsName()
    return lambda == null ||
        name == null ||
        lambda.anyDescendantOfType<KtReturnExpression> { it.getLabelNameAsName() == name }
}

context(session: KaSession)
internal fun KaFunctionCall<*>.acceptsSamConversion(
    argument: KtValueArgument,
    samCall: KtCallExpression,
): Boolean =
    with(session) {
        val samConstructor = samCall.resolveToCall()?.successfulFunctionCallOrNull()
        val symbol = samConstructor?.symbol as? KaSamConstructorSymbol
        val parameter = valueArgumentMapping[argument.getArgumentExpression()]
        samConstructor != null &&
            symbol != null &&
            parameter != null &&
            !symbol.returnType.isMarkedNullable &&
            parameter.symbol.returnType.symbol != null &&
            samConstructor.signature.returnType.semanticallyEquals(parameter.returnType.withNullability(false))
    }

internal fun KtCallExpression.canDropSamConstructors(convertible: Map<KtValueArgument, KtCallExpression>): Boolean {
    val container = samCallContainer()
    val text = convertible.entries
        .sortedByDescending { (argument, _) -> argument.getStartOffsetIn(container) }
        .fold(container.text) { text, (argument, samCall) -> container.replaceSamArgument(text, argument, samCall) }
    val fragment = KtPsiFactory(project).createBlockCodeFragment(text, this)
    val newCall = fragment.findElementAt(getStartOffsetIn(container))?.parentOfType<KtCallExpression>()
    val identity = resolvedFunctionIdentity()
    return identity != null && newCall?.resolvedFunctionIdentity() == identity
}

internal fun KtElement.replaceSamArgument(
    text: String,
    argument: KtValueArgument,
    samCall: KtCallExpression,
): String {
    val expression = argument.getArgumentExpression()
    val lambda = samCall.samLambdaArgument()?.getArgumentExpression()
    if (expression == null || lambda == null) return text
    val start = expression.getStartOffsetIn(this)
    return text.replaceRange(start, start + expression.textLength, lambda.text)
}
