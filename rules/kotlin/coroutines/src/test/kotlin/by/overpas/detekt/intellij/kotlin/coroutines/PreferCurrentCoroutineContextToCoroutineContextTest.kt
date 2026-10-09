package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferCurrentCoroutineContextToCoroutineContextTest {

    private val environment = createEnvironment()

    private val sut = PreferCurrentCoroutineContextToCoroutineContext(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        import kotlin.coroutines.CoroutineContext

        interface Job : CoroutineContext.Element {
            companion object Key : CoroutineContext.Key<Job>
        }

        interface CoroutineScope {
            val coroutineContext: CoroutineContext
        }

        suspend fun currentCoroutineContext(): CoroutineContext = TODO()
    """.trimIndent()

    @Test
    fun `an imported coroutine context usage is reported`() {
        val code = """
            import kotlin.coroutines.coroutineContext

            suspend fun test() {
                coroutineContext
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported coroutine context used as a receiver is reported`() {
        val code = """
            import kotlin.coroutines.coroutineContext

            suspend fun test() {
                coroutineContext.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported coroutine context used with the get operator is reported`() {
        val code = """
            import kotlin.coroutines.coroutineContext
            import kotlinx.coroutines.Job

            suspend fun test() {
                coroutineContext[Job]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a fully qualified coroutine context usage is reported`() {
        val code = """
            suspend fun test() {
                kotlin.coroutines.coroutineContext
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a fully qualified coroutine context used as a receiver is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test() {
                kotlin.coroutines.coroutineContext.toString()
                kotlin.coroutines.coroutineContext[Job]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(2, findings.size)
    }

    @Test
    fun `the coroutine scope context on an explicit receiver passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            suspend fun test(scope: CoroutineScope) {
                scope.coroutineContext
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the coroutine scope context on an implicit receiver passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope

            fun CoroutineScope.test() {
                coroutineContext
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an aliased coroutine context import and usage pass`() {
        val code = """
            import kotlin.coroutines.coroutineContext as ctx

            suspend fun test() {
                ctx
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
