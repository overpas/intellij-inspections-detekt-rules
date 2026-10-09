package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class DeferredResultUnusedTest {

    private val environment = createEnvironment()

    private val sut = DeferredResultUnused(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext

        interface CoroutineScope {
            val coroutineContext: CoroutineContext
        }

        interface Deferred<out T> {
            suspend fun await(): T
        }

        fun <T> CoroutineScope.async(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T,
        ): Deferred<T> = TODO()

        fun <T> runBlocking(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T,
        ): T = TODO()
    """.trimIndent()

    @Test
    fun `an unused async call is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.async

            fun CoroutineScope.test() {
                async { 42 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused deferred from a method is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            class User

            interface DbHandler {
                fun getUser(id: Long): Deferred<User>
                fun doStuff(): Deferred<Unit>
            }

            fun DbHandler.test() {
                getUser(42L)
                doStuff()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a generic builder that returns an unused deferred is reported`() {
        val code = """
            import kotlinx.coroutines.async
            import kotlinx.coroutines.runBlocking

            fun test() {
                runBlocking {
                    async { 42 }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an async call assigned to a variable passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.async

            fun CoroutineScope.test() {
                val result = async { 13 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an async call passed as an argument passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Deferred
            import kotlinx.coroutines.async

            fun useIt(deferred: Deferred<Int>) {}

            fun CoroutineScope.test() {
                useIt(async { 7 })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an awaited deferred passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Deferred
            import kotlinx.coroutines.async

            interface DbHandler {
                fun doStuff(): Deferred<Unit>
            }

            suspend fun CoroutineScope.test(handler: DbHandler) {
                async { 3 }.await()
                handler.doStuff().await()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an async call used with operators passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Deferred
            import kotlinx.coroutines.async

            operator fun Deferred<Int>.plus(arg: Int) = this

            operator fun Deferred<Int>.unaryPlus() = this

            fun CoroutineScope.test() {
                async { -1 } + 1
                +(async { 0 })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a deferred passed to a null check function passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Deferred
            import kotlinx.coroutines.async

            fun CoroutineScope.test() {
                val first: Deferred<Int>? = async { 42 }
                val second: Deferred<Int>? = async { 42 }
                requireNotNull(first)
                checkNotNull(second)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused call that returns another type passes`() {
        val code = """
            fun produce(): Int = 42

            fun test() {
                produce()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
