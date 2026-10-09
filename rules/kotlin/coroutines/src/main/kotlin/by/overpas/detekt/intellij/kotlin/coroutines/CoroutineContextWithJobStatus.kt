package by.overpas.detekt.intellij.kotlin.coroutines

import org.jetbrains.kotlin.psi.KtExpression

internal sealed interface CoroutineContextWithJobStatus {

    fun append(other: CoroutineContextWithJobStatus): CoroutineContextWithJobStatus =
        other as? WithJob ?: this

    data object Unknown : CoroutineContextWithJobStatus

    data object NoJob : CoroutineContextWithJobStatus

    class WithJob(
        val source: KtExpression,
        val isCancellable: Boolean,
    ) : CoroutineContextWithJobStatus {

        fun messageFor(builder: String): String =
            if (isCancellable) {
                "Passing 'CoroutineContext' with a 'Job' to '$builder' builder " +
                    "can lead to structured concurrency violations"
            } else {
                "'NonCancellable' object should not be used with '$builder' builder, " +
                    "it violates structured concurrency"
            }
    }
}
