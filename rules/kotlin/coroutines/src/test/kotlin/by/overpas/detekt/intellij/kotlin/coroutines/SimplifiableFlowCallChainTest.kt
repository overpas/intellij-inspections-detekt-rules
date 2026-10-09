package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifiableFlowCallChainTest {

    private val environment = createEnvironment()

    private val sut = SimplifiableFlowCallChain(Config.empty)

    @Test
    fun `a filter call followed by first is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter
            import kotlinx.coroutines.flow.first

            suspend fun test(flow: Flow<Int>) {
                flow.filter { it != 0 }.first()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter call followed by firstOrNull is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter
            import kotlinx.coroutines.flow.firstOrNull

            suspend fun test(flow: Flow<Int>) {
                flow.filter { it != 0 }.firstOrNull()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter call followed by count is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.count
            import kotlinx.coroutines.flow.filter

            suspend fun test(flow: Flow<Int>) {
                flow.filter { it != 0 }.count()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map call followed by filterNotNull is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filterNotNull
            import kotlinx.coroutines.flow.map

            fun test(flow: Flow<Int>) {
                flow.map { if (it != 0) it else null }.filterNotNull()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a chain on a flow factory call is reported`() {
        val code = """
            import kotlinx.coroutines.flow.filter
            import kotlinx.coroutines.flow.first
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() = flowOf(1, 2).filter { it > 1 }.first()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter call followed by first with a predicate passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter
            import kotlinx.coroutines.flow.first

            suspend fun test(flow: Flow<Int>) {
                flow.filter { it != 0 }.first { it > 1 }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter call with a return in its lambda passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter
            import kotlinx.coroutines.flow.first

            suspend fun test(flow: Flow<Int>) {
                flow.filter {
                    if (it == 0) return@filter false
                    true
                }.first()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map call followed by first passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.first
            import kotlinx.coroutines.flow.map

            suspend fun test(flow: Flow<Int>) {
                flow.map { it + 1 }.first()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter call followed by first on a list passes`() {
        val code = """
            fun test(list: List<Int>) {
                list.filter { it != 0 }.first()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            suspend fun <T> Flow<T>.first(): T = TODO()

            suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T = TODO()

            suspend fun <T> Flow<T>.firstOrNull(): T? = TODO()

            suspend fun <T> Flow<T>.count(): Int = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
