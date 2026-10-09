package by.overpas.detekt.intellij.kotlin.styleissues

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.unpackFunctionLiteral

internal fun KtCallExpression.movableTrailingLambda(): KtLambdaExpression? {
    val arguments = valueArguments
    val lambda = arguments.lastOrNull()?.getArgumentExpression()?.unpackFunctionLiteral()
    val isComplex = arguments.lastOrNull()?.isNamed() == true ||
        arguments.count { it.getArgumentExpression()?.unpackFunctionLiteral() != null } > 1
    val isInDelegation = getStrictParentOfType<KtDelegatedSuperTypeEntry>() != null
    return lambda?.takeIf { lambdaArguments.isEmpty() && !isComplex && !isInDelegation }
}
