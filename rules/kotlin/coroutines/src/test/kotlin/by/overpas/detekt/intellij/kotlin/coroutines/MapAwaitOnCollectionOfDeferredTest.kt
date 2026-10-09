package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MapAwaitOnCollectionOfDeferredTest {

    private val environment = createEnvironment()

    private val sut = MapAwaitOnCollectionOfDeferred(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        interface Job

        interface Deferred<out T> : Job {
            suspend fun await(): T
        }
    """.trimIndent()

    private val flow = """
        package kotlinx.coroutines.flow

        interface Flow<out T>

        fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R> = TODO()
    """.trimIndent()

    @Test
    fun `map awaiting a list of deferred values is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>) {
                val results = deferreds.map { it.await() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `map awaiting with a named parameter is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<String>>) {
                val results = deferreds.map { deferred -> deferred.await() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `map with a labeled return of the awaited value is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>) {
                val results = deferreds.map {
                    return@map it.await()
                }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `map awaiting on an implicit receiver is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun <T> Collection<Deferred<T>>.test(): List<T> {
                return map { it.await() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `map awaiting an array of deferred values passes`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: Array<Deferred<String>>) {
                val results = deferreds.map { it.await() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `map awaiting a flow of deferred values passes`() {
        val code = """
            import kotlinx.coroutines.Deferred
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.map

            fun test(deferreds: Flow<Deferred<String>>) {
                val results = deferreds.map { it.await() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines, flow)

        assertEquals(0, findings.size)
    }

    @Test
    fun `map with several statements passes`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>) {
                val results = deferreds.map {
                    println("hello!")
                    it.await()
                }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `map with a call on the awaited value passes`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>) {
                val results = deferreds.map { it.await().toString() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `map with a non-local return passes`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>): Int {
                deferreds.map {
                    return it.await()
                }
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `map awaiting another deferred passes`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(deferreds: List<Deferred<Int>>, other: Deferred<Int>) {
                val results = deferreds.map { other.await() }
                println(results)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
