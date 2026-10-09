package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

internal data class SimplifyNestedEachInScopeFunctionCall(
    val scopeCall: KtCallExpression,
    val lambda: KtLambdaExpression,
    val statement: KtExpression,
    val labelName: String,
) {
    val eachCall: KtCallExpression? =
        if (statement is KtDotQualifiedExpression) {
            statement.selectorExpression as? KtCallExpression
        } else {
            statement as? KtCallExpression
        }

    val eachLambdaBody: KtExpression? =
        eachCall
            ?.valueArguments
            ?.singleOrNull()
            ?.getArgumentExpression()
            .unpackedNestedEachLambda()
            ?.bodyExpression
}
