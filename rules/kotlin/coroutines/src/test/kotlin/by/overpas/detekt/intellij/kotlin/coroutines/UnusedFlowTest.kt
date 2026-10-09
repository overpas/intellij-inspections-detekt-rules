package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedFlowTest {

    private val environment = createEnvironment()

    private val sut = UnusedFlow(Config.empty)

    @Test
    fun `a flow call statement is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf

            fun test() {
                flowOf(1)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused call chain on a flow variable is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.map

            suspend fun test() {
                val a = flowOf(1)
                a.map { it * 2 }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused flow variable reference is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() {
                val a = flowOf(1)
                a
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flow statement that is not the last statement of an if branch is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() {
                val a = if (1 == 1) {
                    flowOf(1)
                    flowOf(1)
                } else {
                    flowOf(2)
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flow statement in a lambda that returns Unit is reported`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.map

            fun test() {
                listOf(1).forEach {
                    flowOf(1).map { it * 2 }
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flow statement that is not the last statement of a flatMapConcat lambda is reported`() {
        val code = """
            import kotlinx.coroutines.flow.collect
            import kotlinx.coroutines.flow.flatMapConcat
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() {
                flowOf(1).flatMapConcat {
                    flowOf(1)
                    flowOf(1)
                }.collect {
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused flow operator call is reported`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf

            operator fun Flow<Int>.plus(other: Flow<Int>): Flow<Int> = flowOf(1, 2, 3)

            suspend fun foo() {
                flowOf(1, 2, 3) + flowOf(1, 2, 3)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a collected flow passes`() {
        val code = """
            import kotlinx.coroutines.flow.collect
            import kotlinx.coroutines.flow.flow
            import kotlinx.coroutines.flow.map

            suspend fun test() {
                flow {
                    emit(5)
                }.map { it * 2 }.collect { println(it) }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow in a property initializer passes`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf
            import kotlinx.coroutines.flow.map

            fun test() {
                val a = flowOf(1).map { it * 2 }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow that is assigned to a property passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf

            class Foo {
                val someFlow: Flow<Int>

                init {
                    someFlow = flowOf(1, 2, 3)
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned flow passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf

            fun test(): Flow<Int> {
                val a = flowOf(1)
                return (a)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow passed to a function passes`() {
        val code = """
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf

            fun consumer(a: Flow<Int>) {
            }

            suspend fun test() {
                consumer(flowOf(1))
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow that is the result of a lambda passes`() {
        val code = """
            import kotlinx.coroutines.flow.collect
            import kotlinx.coroutines.flow.flatMapConcat
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() {
                flowOf(1).flatMapConcat {
                    flowOf(1)
                }.collect {
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow that is the result of a when branch passes`() {
        val code = """
            import kotlinx.coroutines.flow.flowOf

            suspend fun test() {
                val a = when {
                    1 == 1 -> {
                        flowOf(1)
                    }
                    else -> {
                        flowOf(2)
                    }
                }
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flow type and a named argument pass`() {
        val code = """
            import kotlinx.coroutines.flow.Flow

            open class B(alsoFlow: Flow<Int>)

            class C(flow: Flow<Int>) : B(alsoFlow = flow)
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface FlowCollector<in T> {
                suspend fun emit(value: T)
            }

            interface Flow<out T> {
                suspend fun collect(collector: FlowCollector<T>)
            }

            fun <T> flowOf(vararg elements: T): Flow<T> = TODO()

            fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

            inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = TODO()

            fun <T, R> Flow<T>.flatMapConcat(transform: suspend (value: T) -> Flow<R>): Flow<R> = TODO()

            suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (value: T) -> Unit): Unit = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
