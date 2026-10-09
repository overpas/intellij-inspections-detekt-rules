package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifiableFlowCallTest {

    private val environment = createEnvironment()

    private val sut = SimplifiableFlowCall(Config.empty)

    private val flow = """
        package kotlinx.coroutines.flow

        interface Flow<out T>

        inline fun <T> Flow<T>.filter(crossinline predicate: suspend (T) -> Boolean): Flow<T> = TODO()

        fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

        fun <T, R> Flow<T>.flatMapMerge(
            concurrency: Int = 16,
            transform: suspend (value: T) -> Flow<R>,
        ): Flow<R> = TODO()
    """.trimIndent()

    @Test
    fun `flatMapMerge with an identity lambda is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flatMapMerge

            fun test(flow: Flow<Flow<Int>>) {
                flow.flatMapMerge { it }
                flow.flatMapMerge { f -> f }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(2, findings.size)
    }

    @Test
    fun `flatMapMerge with a concurrency and an identity lambda is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flatMapMerge

            fun test(flow: Flow<Flow<Int>>) {
                flow.flatMapMerge(10) { it }
                flow.flatMapMerge(concurrency = 10) { it }
                flow.flatMapMerge(transform = { it }, concurrency = 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(3, findings.size)
    }

    @Test
    fun `flatMapConcat with an identity lambda is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flatMapConcat

            fun test(flow: Flow<Flow<Int>>) {
                flow.flatMapConcat { it }
                flow.flatMapConcat { f -> f }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(2, findings.size)
    }

    @Test
    fun `filter with a not null check is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter

            fun test(flow: Flow<String?>) {
                flow.filter { it != null }
                flow.filter { value -> null !== value }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(2, findings.size)
    }

    @Test
    fun `filter with a type check is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter

            fun test(flow: Flow<Any>) {
                flow.filter { it is String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(1, findings.size)
    }

    @Test
    fun `flatMapMerge with a transforming lambda passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flatMapMerge

            fun test(flow: Flow<Flow<Int>>, other: Flow<Int>) {
                flow.flatMapMerge { other }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filter with a negated type check passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter

            fun test(flow: Flow<Any>) {
                flow.filter { it !is String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filter with another condition passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.filter

            fun test(flow: Flow<String?>, other: String?) {
                flow.filter { other != null }
                flow.filter { it == null }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(0, findings.size)
    }

    @Test
    fun `filter on a collection passes`() {
        val code = """
            fun test(list: List<String?>) {
                list.filter { it != null }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, flow)

        assertEquals(0, findings.size)
    }
}
