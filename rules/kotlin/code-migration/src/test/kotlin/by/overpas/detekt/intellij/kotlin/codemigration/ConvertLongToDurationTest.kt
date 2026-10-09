package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertLongToDurationTest {

    private val environment = createEnvironment()

    private val sut = ConvertLongToDuration(Config.empty)

    @Test
    fun `a delay call with a long literal is reported`() {
        val code = """
            import kotlinx.coroutines.delay

            suspend fun test() {
                delay(42)
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines

            import kotlin.time.Duration

            suspend fun delay(timeMillis: Long) {}

            suspend fun delay(duration: Duration) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delay call with a long variable is reported`() {
        val code = """
            import kotlinx.coroutines.delay

            suspend fun test() {
                val delayMillis = 42L
                delay(delayMillis)
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines

            import kotlin.time.Duration

            suspend fun delay(timeMillis: Long) {}

            suspend fun delay(duration: Duration) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delay call with an expression is reported`() {
        val code = """
            import kotlinx.coroutines.delay

            suspend fun test(base: Long) {
                delay(base + 100)
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines

            import kotlin.time.Duration

            suspend fun delay(timeMillis: Long) {}

            suspend fun delay(duration: Duration) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sample call on a flow is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.sample

            fun test(flow: Flow<Int>) = flow.sample(200)
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> = this

            fun <T> Flow<T>.debounce(timeoutMillis: (T) -> Long): Flow<T> = this

            fun <T> Flow<T>.sample(periodMillis: Long): Flow<T> = this
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an onTimeout call with a trailing lambda is reported`() {
        val code = """
            import kotlinx.coroutines.selects.SelectBuilder
            import kotlinx.coroutines.selects.onTimeout

            fun test(builder: SelectBuilder<Unit>) {
                builder.onTimeout(1000) {
                    println()
                }
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines.selects

            interface SelectBuilder<in R>

            fun <R> SelectBuilder<R>.onTimeout(timeMillis: Long, block: suspend () -> R) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delay call with a duration passes`() {
        val code = """
            import kotlin.time.Duration.Companion.milliseconds
            import kotlinx.coroutines.delay

            suspend fun test() {
                delay(42.milliseconds)
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines

            import kotlin.time.Duration

            suspend fun delay(timeMillis: Long) {}

            suspend fun delay(duration: Duration) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delay call with a named argument passes`() {
        val code = """
            import kotlinx.coroutines.delay

            suspend fun test() {
                delay(timeMillis = 1000)
            }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines

            import kotlin.time.Duration

            suspend fun delay(timeMillis: Long) {}

            suspend fun delay(duration: Duration) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a debounce call with a lambda passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.debounce

            fun test(flow: Flow<Int>) = flow.debounce { 100L }
        """.trimIndent()
        val dependency = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> = this

            fun <T> Flow<T>.debounce(timeoutMillis: (T) -> Long): Flow<T> = this

            fun <T> Flow<T>.sample(periodMillis: Long): Flow<T> = this
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local function named delay passes`() {
        val code = """
            fun delay(timeMillis: Long) {}

            fun test() {
                delay(1000)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
