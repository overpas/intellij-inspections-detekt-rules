package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspendCoroutineLacksCancellationGuaranteesTest {

    private val environment = createEnvironment()

    private val sut = SuspendCoroutineLacksCancellationGuarantees(Config.empty)

    @Test
    fun `a suspendCoroutine call with a lambda is reported`() {
        val code = """
            import kotlin.coroutines.suspendCoroutine

            suspend fun foo(): String {
                return suspendCoroutine {}
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a suspendCoroutine call with an explicit type argument is reported`() {
        val code = """
            import kotlin.coroutines.suspendCoroutine

            suspend fun bar() {
                suspendCoroutine<Unit> { continuation -> }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a fully qualified suspendCoroutine call is reported`() {
        val code = """
            suspend fun usage(): String {
                return kotlin.coroutines.suspendCoroutine { }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a suspendCoroutine call with a labeled return is reported`() {
        val code = """
            import kotlin.coroutines.suspendCoroutine

            suspend fun quux(): String {
                return suspendCoroutine {
                    return@suspendCoroutine
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a suspendCoroutine call with an anonymous function is reported`() {
        val code = """
            import kotlin.coroutines.suspendCoroutine

            suspend fun baz(): String {
                return suspendCoroutine(sc@fun (c) {
                    return@sc
                })
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a suspendCoroutine call with a function argument is reported`() {
        val code = """
            import kotlin.coroutines.Continuation
            import kotlin.coroutines.suspendCoroutine

            suspend fun foo(action: (Continuation<String>) -> Unit): String {
                return suspendCoroutine(action)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `calls inside the suspendCoroutine lambda pass`() {
        val code = """
            import kotlin.coroutines.suspendCoroutine

            suspend fun foo(): String {
                return suspendCoroutine {
                    run {}
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a user-defined suspendCoroutine function passes`() {
        val code = """
            fun suspendCoroutine(block: () -> Unit) = block()

            fun foo() {
                suspendCoroutine {}
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a suspendCancellableCoroutine call passes`() {
        val code = """
            import kotlinx.coroutines.suspendCancellableCoroutine

            suspend fun foo(): String {
                return suspendCancellableCoroutine {}
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.Continuation

            interface CancellableContinuation<in T> : Continuation<T>

            suspend fun <T> suspendCancellableCoroutine(block: (CancellableContinuation<T>) -> Unit): T = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
