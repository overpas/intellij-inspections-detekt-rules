package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UselessCallOnFlowTest {

    private val environment = createEnvironment()

    private val sut = UselessCallOnFlow(Config.empty)

    @Test
    fun `a filterNotNull call on a flow of non-null values is reported`() {
        val code = """
            import kotlinx.coroutines.flow.filterNotNull
            import kotlinx.coroutines.flow.flowOf

            val x = flowOf("1").filterNotNull()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterNotNull call on a flow parameter of non-null values is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filterNotNull

            fun test(flow: Flow<Int>) = flow.filterNotNull()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterNotNull call on a flow of nullable values passes`() {
        val code = """
            import kotlinx.coroutines.flow.filterNotNull
            import kotlinx.coroutines.flow.flowOf

            val x = flowOf("1", null).filterNotNull()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call of the exact element type is reported`() {
        val code = """
            import kotlinx.coroutines.flow.filterIsInstance
            import kotlinx.coroutines.flow.flowOf

            val x = flowOf("1").filterIsInstance<String>()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call of a supertype of the element type is reported`() {
        val code = """
            import kotlinx.coroutines.flow.filterIsInstance
            import kotlinx.coroutines.flow.flowOf

            val x = flowOf("1").filterIsInstance<CharSequence>()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call on a flow of mixed values passes`() {
        val code = """
            import kotlinx.coroutines.flow.filterIsInstance
            import kotlinx.coroutines.flow.flowOf

            val x = flowOf(true, "1").filterIsInstance<String>()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapNotNull call with a lambda that returns non-null values is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.mapNotNull

            val x = flowOf("1").mapNotNull { it.toInt() }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapNotNull call with a function reference that returns non-null values is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.mapNotNull

            fun parse(value: String): Int = value.toInt()

            val x = flowOf("1").mapNotNull(::parse)
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapNotNull call with a lambda that can return null passes`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.mapNotNull

            val x = flowOf("1").mapNotNull { if (it.isNotEmpty()) it.toInt() else null }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapNotNull call with a labeled return of null passes`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.mapNotNull

            val x = flowOf("1").mapNotNull {
                if (it.isEmpty()) return@mapNotNull null
                it.toInt()
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterNotNull call on a list passes`() {
        val code = """
            val x = listOf("1").filterNotNull()
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> = TODO()

            inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = TODO()

            inline fun <T, R : Any> Flow<T>.mapNotNull(crossinline transform: suspend (value: T) -> R?): Flow<R> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
