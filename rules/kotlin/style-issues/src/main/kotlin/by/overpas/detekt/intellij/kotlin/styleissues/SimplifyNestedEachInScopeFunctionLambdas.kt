package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiUtil

internal const val SIMPLIFY_NESTED_EACH_ALSO = "also"

internal const val SIMPLIFY_NESTED_EACH_APPLY = "apply"

internal const val SIMPLIFY_NESTED_EACH_FOR_EACH = "forEach"

internal const val SIMPLIFY_NESTED_EACH_ON_EACH = "onEach"

internal fun KtExpression?.unpackedNestedEachLambda(): KtLambdaExpression? =
    KtPsiUtil.deparenthesize(this) as? KtLambdaExpression

internal fun KtCallExpression.nestedEachInScopeFunction(): SimplifyNestedEachInScopeFunctionCall? {
    val scopeName = calleeExpression?.text?.takeIf {
        it == SIMPLIFY_NESTED_EACH_ALSO || it == SIMPLIFY_NESTED_EACH_APPLY
    }
    val argument = (valueArguments.singleOrNull() as? KtLambdaArgument)?.getArgumentExpression()
    val label = generateSequence(argument) { (it as? KtAnnotatedExpression)?.baseExpression }
        .firstNotNullOfOrNull { (it as? KtLabeledExpression)?.getLabelName() }
    val lambda = argument.unpackedNestedEachLambda()
    val statement = lambda?.bodyExpression?.run { statements.singleOrNull() }
    val nested = statement?.let { single ->
        SimplifyNestedEachInScopeFunctionCall(
            scopeCall = this,
            lambda = lambda,
            statement = single,
            labelName = label ?: scopeName.orEmpty(),
        )
    }
    val eachName = nested?.eachCall?.calleeExpression?.text
    val isEachCall = eachName == SIMPLIFY_NESTED_EACH_FOR_EACH || eachName == SIMPLIFY_NESTED_EACH_ON_EACH
    return nested?.takeIf { scopeName != null && isEachCall }
}
