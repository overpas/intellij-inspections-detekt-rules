package by.overpas.detekt.intellij.kotlin.javainterop

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

internal fun KtCallExpression.hasMutatorArguments(methodName: String): Boolean =
    when (methodName) {
        "fill" -> valueArguments.size == 2

        "reverse" -> valueArguments.size == 1

        "shuffle" -> valueArguments.size in 1..2

        "sort" ->
            valueArguments.size == 1 || (valueArguments.size == 2 && hasLambdaComparator())

        else -> false
    }

private fun KtCallExpression.hasLambdaComparator(): Boolean =
    valueArguments[1].getArgumentExpression() is KtLambdaExpression
