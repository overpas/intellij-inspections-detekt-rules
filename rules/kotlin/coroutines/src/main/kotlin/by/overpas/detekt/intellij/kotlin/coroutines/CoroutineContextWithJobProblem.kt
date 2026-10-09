package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.psi.KtExpression

internal data class CoroutineContextWithJobProblem(
    val source: KtExpression,
    val message: String,
)
