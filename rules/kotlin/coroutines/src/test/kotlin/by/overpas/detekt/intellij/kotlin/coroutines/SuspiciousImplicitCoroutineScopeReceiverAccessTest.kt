package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousImplicitCoroutineScopeReceiverAccessTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousImplicitCoroutineScopeReceiverAccess(Config.empty)

    @Test
    fun `an extension call on the outer scope inside a suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of the outer scope inside a suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    coroutineContext
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a generic extension call on the outer scope inside a suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun <T : CoroutineScope> T.doGenericStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    doGenericStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension call inside a crossinline suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend inline fun suspendInlineWrapper(crossinline action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendInlineWrapper {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension call inside a suspend fun interface lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            fun interface CustomSuspendAction {
                suspend fun customInvoke()
            }

            private suspend fun suspendWrapper(action: CustomSuspendAction) {
                action.customInvoke()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension call on a lambda receiver scope inside a suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.coroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun test() {
                coroutineScope {
                    suspendWrapper {
                        doStuff()
                    }
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension call on a labeled lambda receiver scope inside a suspending lambda is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.coroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun test() {
                coroutineScope customLabel@{
                    suspendWrapper {
                        doStuff()
                    }
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension call directly in a scope extension function passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                doStuff()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call inside an inlined suspending lambda passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend inline fun suspendInlineWrapper(action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendInlineWrapper {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call with an explicit receiver passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    this.doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call inside a non-suspending fun interface lambda passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            fun interface CustomLambda {
                fun customInvoke()
            }

            private suspend fun suspendWrapper(action: CustomLambda) {
                action.customInvoke()
            }

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unrelated extension call inside a suspending lambda passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            private suspend fun suspendWrapper(action: suspend () -> Unit) {
                action()
            }

            fun Any.doUnrelatedStuff() {}

            suspend fun CoroutineScope.test() {
                suspendWrapper {
                    doUnrelatedStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call directly in a coroutineScope lambda passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.coroutineScope

            fun CoroutineScope.doStuff() {}

            suspend fun test() {
                coroutineScope {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call on a scope subtype receiver passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            fun CoroutineScope.doStuff() {}

            abstract class MyCoroutineScopeBase : CoroutineScope {
                suspend fun foo() {
                    doStuff()
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an extension call inside a select clause passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.async
            import kotlinx.coroutines.selects.select

            fun CoroutineScope.doStuff() {}

            suspend fun CoroutineScope.test() {
                val deferred = async { 42 }

                select<Unit> {
                    deferred.onAwait {
                        doStuff()
                    }
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines

            import kotlin.coroutines.CoroutineContext
            import kotlinx.coroutines.selects.SelectClause1

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            interface Deferred<out T> {
                val onAwait: SelectClause1<T>
            }

            fun <T> CoroutineScope.async(block: suspend CoroutineScope.() -> T): Deferred<T> = TODO()
        """.trimIndent()
        val selects = """
            package kotlinx.coroutines.selects

            interface SelectClause1<out Q>

            interface SelectBuilder<in R> {
                operator fun <Q> SelectClause1<Q>.invoke(block: suspend (Q) -> R)
            }

            suspend inline fun <R> select(crossinline builder: SelectBuilder<R>.() -> Unit): R = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines, selects)

        assertEquals(0, findings.size)
    }
}
