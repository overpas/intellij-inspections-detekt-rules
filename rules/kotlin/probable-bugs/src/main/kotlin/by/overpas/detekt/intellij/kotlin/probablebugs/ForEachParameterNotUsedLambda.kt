package by.overpas.detekt.intellij.kotlin.probablebugs

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private const val FOR_EACH = "forEach"

private val FOR_EACH_CALLABLE_IDS = listOf("kotlin.collections", "kotlin.sequences", "kotlin.text")
    .map { CallableId(FqName(it), Name.identifier(FOR_EACH)) }

internal fun KtCallExpression.unusedForEachParameterName(): String? {
    val lambda = lambdaArguments.singleOrNull()?.getLambdaExpression()?.takeIf { it.functionLiteral.arrow == null }
    val isForEach = (calleeExpression as? KtNameReferenceExpression)?.getReferencedName() == FOR_EACH
    if (lambda == null || !isForEach) return null
    return analyze(this) {
        val parameter = lambda.functionLiteral.symbol.valueParameters.singleOrNull()
        val isStdlibForEach =
            resolveToCall()?.successfulFunctionCallOrNull()?.run { symbol.callableId } in FOR_EACH_CALLABLE_IDS
        parameter?.takeIf { isStdlibForEach && !lambda.uses(it) }?.run { name.asString() }
    }
}

context(session: KaSession)
private fun KtLambdaExpression.uses(parameter: KaValueParameterSymbol): Boolean =
    with(session) {
        bodyExpression
            ?.collectDescendantsOfType<KtNameReferenceExpression> { it.getReferencedNameAsName() == parameter.name }
            .orEmpty()
            .flatMap { it.references.filterIsInstance<KtReference>() }
            .any { it.resolveToSymbol() == parameter }
    }
