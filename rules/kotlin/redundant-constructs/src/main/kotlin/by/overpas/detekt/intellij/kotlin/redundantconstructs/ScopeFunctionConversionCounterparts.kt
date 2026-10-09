@file:OptIn(KaExperimentalApi::class)

package by.overpas.detekt.intellij.kotlin.redundantconstructs

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal const val SCOPE_FUNCTION_WITH = "with"

internal fun KtNameReferenceExpression.isScopeFunctionCallee(): Boolean =
    analyze(this) {
        val symbol = resolveSymbol() as? KaCallableSymbol
        symbol != null && (symbol.receiverParameter != null || getReferencedName() == SCOPE_FUNCTION_WITH)
    }

internal fun KtLambdaExpression.isSimpleScopeFunctionLambda(): Boolean {
    val parameter = valueParameters.singleOrNull()?.takeIf { it.destructuringDeclaration == null }
    val body = bodyExpression?.takeUnless { it.anyDescendantOfType<KtLambdaExpression>() }
    if (parameter == null || body == null) return false
    val templateReceivers = body.collectDescendantsOfType<KtStringTemplateEntryWithExpression>()
        .mapNotNull { (it.expression as? KtDotQualifiedExpression)?.receiverExpression as? KtNameReferenceExpression }
    return templateReceivers.isEmpty() ||
        analyze(this) { templateReceivers.none { it.resolveSymbol()?.psi == parameter } }
}

internal fun KtCallExpression.isValidScopeCounterpart(
    calleeName: String,
    name: String,
): Boolean {
    val receiver = (parent as? KtQualifiedExpression)?.receiverExpression
    val isNullableReceiver = name == SCOPE_FUNCTION_WITH &&
        receiver != null &&
        analyze(receiver) { receiver.expressionType?.isNullable == true }
    return !isNullableReceiver && resolvesToStdlibScopeFunction(calleeName, name)
}

private fun KtCallExpression.resolvesToStdlibScopeFunction(
    calleeName: String,
    name: String,
): Boolean {
    val content = scopeFunctionFragmentSource(calleeName, name)?.let { (text, context) ->
        KtPsiFactory(project).createExpressionCodeFragment(text, context).getContentElement()
    }
    return content != null &&
        analyze(content) {
            val callableId = scopeCallableOf(content)?.callableId
            callableId?.packageName == StandardClassIds.BASE_KOTLIN_PACKAGE &&
                callableId.callableName.asString() == name
        }
}

private fun KaSession.scopeCallableOf(content: KtElement): KaCallableSymbol? =
    if (content is KtDotQualifiedExpression) {
        (content.resolveToCallCandidates().singleOrNull()?.candidate as? KaFunctionCall<*>)?.symbol
    } else {
        content.resolveToCall()?.successfulFunctionCallOrNull()?.symbol
    }
