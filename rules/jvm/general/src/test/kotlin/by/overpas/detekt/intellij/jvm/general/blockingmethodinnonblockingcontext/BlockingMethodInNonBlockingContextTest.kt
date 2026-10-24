package by.overpas.detekt.intellij.jvm.general.blockingmethodinnonblockingcontext

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockingMethodInNonBlockingContextTest {

    private val environment = createEnvironment()

    private val sut = BlockingMethodInNonBlockingContext(Config.empty)

    @Test
    fun `a blocking call in a suspend function is reported`() {
        val code = """
            suspend fun example() {
                Thread.sleep(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a blocking call in a regular function passes`() {
        val code = """
            fun example() {
                Thread.sleep(2)
                run {
                    Thread.sleep(3)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `blocking calls in inlined lambdas of a suspend function are reported`() {
        val code = """
            suspend fun example() {
                run(fun() {
                    Thread.sleep(4)
                })
                run {
                    Thread.sleep(5)
                }
                run {
                    listOf(1).forEach {
                        Thread.sleep(6)
                    }
                }
                "text".let {
                    Thread.sleep(7)
                }
                Array(1) { index ->
                    Thread.sleep(8)
                    index.toString()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(5, findings.size)
    }

    @Test
    fun `blocking calls in lambdas that are not inlined into a suspend function pass`() {
        val code = """
            suspend fun example() {
                customFunction {
                    Thread.sleep(10)
                }
                customFunction {
                    run {
                        Thread.sleep(11)
                    }
                }
                withNoinlineBlock {
                    Thread.sleep(14)
                }
                withCrossinlineBlock {
                    Thread.sleep(15)
                }
                fun local() {
                    Thread.sleep(17)
                }
                val fn = { Thread.sleep(19) }
            }

            fun customFunction(action: () -> Unit) {
                action()
            }

            inline fun withNoinlineBlock(noinline block: () -> Unit) {
                customFunction(block)
            }

            inline fun withCrossinlineBlock(crossinline block: () -> Unit) {
                customFunction { block() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `blocking calls in suspend lambdas and local suspend functions are reported`() {
        val code = """
            fun suspendLambda() {
                customSuspendFunction {
                    Thread.sleep(12)
                }
                customSuspendFunction {
                    run {
                        Thread.sleep(13)
                    }
                }
                withCrossinlineSuspendBlock {
                    Thread.sleep(16)
                }
            }

            suspend fun localSuspendFunction() {
                suspend fun local() {
                    Thread.sleep(18)
                }
                local()
            }

            fun customSuspendFunction(action: suspend () -> Unit) {
            }

            inline fun withCrossinlineSuspendBlock(crossinline block: suspend () -> Unit) {
                customSuspendFunction { block() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `blocking calls in lambdas of a suspend function type are reported`() {
        val code = """
            class LambdaAssignmentCheck {
                val returnSuspendProperty: suspend () -> Unit = {
                    Thread.sleep(1)
                }

                fun returnSuspend(): suspend () -> Unit = {
                    Thread.sleep(1)
                }

                fun assignToSuspendType() {
                    val labeled: suspend () -> Unit = lambda@{
                        Thread.sleep(5)
                    }
                }

                fun returnSuspendFromBlockBody(): suspend () -> Unit {
                    return {
                        Thread.sleep(12)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `blocking calls in lambdas of a regular function type pass`() {
        val code = """
            class LambdaAssignmentCheck {
                fun assignToNonSuspendType() {
                    val plain: () -> Unit = {
                        Thread.sleep(9)
                    }
                }

                fun returnNonSuspend(): () -> Unit = {
                    Thread.sleep(10)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `blocking calls in suspend contexts pass when suspend contexts are not non-blocking`() {
        val code = """
            suspend fun example() {
                Thread.sleep(1)
                run {
                    Thread.sleep(5)
                }
            }
        """.trimIndent()
        val sut = BlockingMethodInNonBlockingContext(TestConfig("isSuspendContextNonBlocking" to false))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `blocking calls in suspend contexts are reported when unknown contexts are not blocking`() {
        val code = """
            suspend fun example() {
                Thread.sleep(1)
            }
        """.trimIndent()
        val sut = BlockingMethodInNonBlockingContext(
            TestConfig("isSuspendContextNonBlocking" to false, "isUnknownContextBlocking" to false),
        )

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the dispatchers of withContext decide the context`() {
        val code = """
            import kotlinx.coroutines.CoroutineName
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.withContext

            suspend fun withDispatchers() {
                withContext(Dispatchers.IO) {
                    Thread.sleep(42)
                }
                withContext(Dispatchers.Default) {
                    Thread.sleep(1)
                }
                withContext(Dispatchers.IO + CoroutineName("My coroutine")) {
                    Thread.sleep(42)
                }
                withContext(Dispatchers.IO.plus(CoroutineName("My coroutine 1"))) {
                    Thread.sleep(42)
                }
                withContext(CoroutineName("My coroutine 2").plus(Dispatchers.IO)) {
                    Thread.sleep(42)
                }
                withContext(Dispatchers.IO) {
                    run {
                        Thread.sleep(43)
                    }
                }
                withContext(Dispatchers.Default) {
                    run {
                        Thread.sleep(2)
                    }
                }
            }
        """.trimIndent()
        val expected = listOf(10, 28)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `an unknown dispatcher passes`() {
        val code = """
            import kotlinx.coroutines.withContext
            import kotlin.coroutines.CoroutineContext

            suspend fun withUnknownDispatcher() {
                withContext(getUnknownContext()) {
                    Thread.sleep(3)
                }
            }

            fun getUnknownContext(): CoroutineContext = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unknown dispatcher is reported when unknown contexts are not blocking`() {
        val code = """
            import kotlinx.coroutines.withContext
            import kotlin.coroutines.CoroutineContext

            suspend fun withUnknownDispatcher() {
                withContext(getUnknownContext()) {
                    Thread.sleep(3)
                }
            }

            fun getUnknownContext(): CoroutineContext = TODO()
        """.trimIndent()
        val sut = BlockingMethodInNonBlockingContext(TestConfig("isUnknownContextBlocking" to false))

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an injected dispatcher passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineDispatcher
            import kotlinx.coroutines.withContext

            class DriverFactory(
                private val ioDispatcher: CoroutineDispatcher,
                private val fallbackDispatcher: CoroutineDispatcher?,
            ) {

                suspend fun create(dispatcher: CoroutineDispatcher, isFallback: Boolean) {
                    val localDispatcher = ioDispatcher
                    withContext(ioDispatcher) { Thread.sleep(1) }
                    withContext(dispatcher) { Thread.sleep(2) }
                    withContext(localDispatcher) { Thread.sleep(3) }
                    withContext(this.ioDispatcher) { Thread.sleep(4) }
                    withContext((ioDispatcher)) { Thread.sleep(5) }
                    withContext(fallbackDispatcher!!) { Thread.sleep(6) }
                    withContext(fallbackDispatcher ?: ioDispatcher) { Thread.sleep(7) }
                    withContext(if (isFallback) fallbackDispatcher!! else ioDispatcher) { Thread.sleep(8) }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an injected dispatcher is reported when unknown contexts are not blocking`() {
        val code = """
            import kotlinx.coroutines.CoroutineDispatcher
            import kotlinx.coroutines.withContext

            class DriverFactory(
                private val ioDispatcher: CoroutineDispatcher,
                private val fallbackDispatcher: CoroutineDispatcher?,
            ) {

                suspend fun create(dispatcher: CoroutineDispatcher, isFallback: Boolean) {
                    val localDispatcher = ioDispatcher
                    withContext(ioDispatcher) { Thread.sleep(1) }
                    withContext(dispatcher) { Thread.sleep(2) }
                    withContext(localDispatcher) { Thread.sleep(3) }
                    withContext(this.ioDispatcher) { Thread.sleep(4) }
                    withContext((ioDispatcher)) { Thread.sleep(5) }
                    withContext(fallbackDispatcher!!) { Thread.sleep(6) }
                    withContext(fallbackDispatcher ?: ioDispatcher) { Thread.sleep(7) }
                    withContext(if (isFallback) fallbackDispatcher!! else ioDispatcher) { Thread.sleep(8) }
                }
            }
        """.trimIndent()
        val sut = BlockingMethodInNonBlockingContext(TestConfig("isUnknownContextBlocking" to false))
        val expected = listOf(11, 12, 13, 14, 15, 16, 17, 18)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `executor annotations on the context decide the context`() {
        val code = """
            import kotlinx.coroutines.withContext
            import kotlin.coroutines.CoroutineContext
            import kotlin.coroutines.EmptyCoroutineContext
            import org.jetbrains.annotations.BlockingExecutor
            import org.jetbrains.annotations.NonBlockingExecutor

            suspend fun testFunction() {
                withContext(getContext()) { Thread.sleep(2) }
                withContext(getExplicitlyNonBlockingContext()) {
                    Thread.sleep(4)
                }
            }

            fun getContext(): @BlockingExecutor CoroutineContext = EmptyCoroutineContext

            fun getExplicitlyNonBlockingContext(): @NonBlockingExecutor CoroutineContext = EmptyCoroutineContext
        """.trimIndent()
        val expected = listOf(10)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `flowOn decides the context of a flow`() {
        val code = """
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flow
            import kotlinx.coroutines.flow.flowOn
            import kotlinx.coroutines.flow.map

            fun flowOnMain(): Flow<Int> = flow {
                Thread.sleep(1)
                emit(1)
            }.flowOn(Dispatchers.Main)

            fun flowSimple(): Flow<Int> = flow {
                Thread.sleep(2)
                emit(1)
            }

            fun flowOnIoAndMap(): Flow<Unit> = flow {
                Thread.sleep(4)
                emit(1)
            }.map { Thread.sleep(5) }
                .flowOn(Dispatchers.IO)
        """.trimIndent()
        val expected = listOf(8, 13)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a suspend lambda with a restricted suspension receiver passes`() {
        val code = """
            import kotlin.coroutines.RestrictsSuspension

            suspend fun testFunction() {
                withRestrictedReceiver({ Thread.sleep(0) }, { Thread.sleep(1) })
                withSimpleReceiver({
                    Thread.sleep(2)
                })
            }

            fun withRestrictedReceiver(firstParam: suspend Test1.() -> Unit, secondParam: () -> Unit) {}

            fun withSimpleReceiver(firstParam: suspend Test2.() -> Unit) {}

            @RestrictsSuspension
            class Test1

            class Test2
        """.trimIndent()
        val expected = listOf(6)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `a coroutine scope with an io dispatcher passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.launch

            class IoScope : CoroutineScope {
                override val coroutineContext = Dispatchers.IO

                fun start() {
                    launch {
                        Thread.sleep(1)
                    }
                }
            }

            class DefaultScope : CoroutineScope {
                override val coroutineContext = Dispatchers.Default

                fun start() {
                    launch {
                        Thread.sleep(2)
                    }
                }
            }
        """.trimIndent()
        val expected = listOf(20)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `the dispatcher of an explicit coroutine scope decides the context`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.SupervisorJob
            import kotlinx.coroutines.launch

            fun start() {
                CoroutineScope(Dispatchers.IO).launch {
                    Thread.sleep(1)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    Thread.sleep(2)
                }
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    Thread.sleep(3)
                }
                CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                    Thread.sleep(4)
                }
            }
        """.trimIndent()
        val expected = listOf(11, 17)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `the receiver of the dispatcher invoke operator decides the context`() {
        val code = """
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.invoke

            suspend fun read() {
                Dispatchers.IO {
                    Thread.sleep(1)
                }
                Dispatchers.Default {
                    Thread.sleep(2)
                }
            }
        """.trimIndent()
        val expected = listOf(9)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `the dispatcher of a coroutine scope in a local or private value decides the context`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.launch

            class Service {
                private val ioScope = CoroutineScope(Dispatchers.IO)
                private val defaultScope = CoroutineScope(Dispatchers.Default)

                fun start() {
                    val localScope = CoroutineScope(Dispatchers.IO)
                    ioScope.launch {
                        Thread.sleep(1)
                    }
                    defaultScope.launch {
                        Thread.sleep(2)
                    }
                    localScope.launch {
                        Thread.sleep(3)
                    }
                }
            }
        """.trimIndent()
        val expected = listOf(15)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `annotations mark blocking methods and non-blocking contexts`() {
        val code = """
            import org.jetbrains.annotations.Blocking
            import org.jetbrains.annotations.NonBlocking

            @Blocking
            fun blockingWork() {}

            fun regularWork() {}

            @NonBlocking
            fun nonBlocking() {
                blockingWork()
                regularWork()
            }

            @NonBlocking
            class Handler {
                fun handle() {
                    Thread.sleep(1)
                }

                @Blocking
                fun blockingHandle() {
                    Thread.sleep(2)
                }
            }

            @Blocking
            class Store {
                fun read() {}

                @NonBlocking
                fun cached() {}
            }

            @NonBlocking
            fun useStore(store: Store) {
                store.read()
                store.cached()
            }
        """.trimIndent()
        val expected = listOf(11, 18, 37)

        val lines = sut.lintWithContext(environment, code).map { it.entity.location.source.line }

        assertEquals(expected, lines)
    }

    @Test
    fun `custom annotations mark blocking methods and non-blocking contexts`() {
        val code = """
            package test

            annotation class Slow
            annotation class EventLoop

            @Slow
            fun slowWork() {}

            @EventLoop
            fun onEvent() {
                slowWork()
            }
        """.trimIndent()
        val sut = BlockingMethodInNonBlockingContext(
            TestConfig(
                "blockingAnnotations" to listOf("test.Slow"),
                "nonBlockingAnnotations" to listOf("test.EventLoop"),
            ),
        )

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `io methods of the jdk are blocking and others are not`() {
        val code = """
            import java.io.FileInputStream
            import java.net.URL

            suspend fun read() {
                FileInputStream("file").read()
                URL("https://example.com")
                "text".length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }
}
