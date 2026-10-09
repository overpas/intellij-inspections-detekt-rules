package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RunBlockingInSuspendFunctionTest {

    private val environment = createEnvironment()

    private val sut = RunBlockingInSuspendFunction(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        import kotlin.coroutines.ContinuationInterceptor
        import kotlin.coroutines.AbstractCoroutineContextElement
        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext

        interface Job

        interface CoroutineScope {
            val coroutineContext: CoroutineContext
        }

        abstract class CoroutineDispatcher : AbstractCoroutineContextElement(ContinuationInterceptor)

        object Dispatchers {
            val Default: CoroutineDispatcher = TODO()
        }

        fun CoroutineScope.launch(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> Unit,
        ): Job = TODO()

        fun <T> runBlocking(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T,
        ): T = TODO()
    """.trimIndent()

    @Test
    fun `runBlocking in a suspend function is reported`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            suspend fun code() {}

            suspend fun something() {
                runBlocking {
                    code()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking with several statements in a suspend function is reported`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            suspend fun code(): String = ""

            suspend fun something() {
                val result = runBlocking {
                    println()
                    code()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking with a context in a suspend function is reported`() {
        val code = """
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.runBlocking

            suspend fun code() {}

            suspend fun something() {
                runBlocking(Dispatchers.Default) {
                    code()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking with a type argument in a suspend function is reported`() {
        val code = """
            import kotlinx.coroutines.launch
            import kotlinx.coroutines.runBlocking

            suspend fun main() {
                val value = runBlocking<String> {
                    launch {
                        println("Hello, World!")
                    }
                    "foo"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking in a suspend lambda is reported`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            fun customFunction(block: suspend () -> Unit) {}

            fun main() {
                customFunction {
                    runBlocking {
                        println("Hello")
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking in an inlined lambda inside a suspend lambda is reported`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            fun customFunction(block: suspend () -> Unit) {}

            fun main() {
                customFunction {
                    run {
                        runBlocking {
                            println("Hello")
                        }
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runBlocking in a non-suspend lambda passes`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            fun customFunction(action: () -> Unit) {
                action()
            }

            suspend fun main() {
                customFunction {
                    run {
                        runBlocking {
                            println("Hello")
                        }
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `runBlocking in a regular function passes`() {
        val code = """
            import kotlinx.coroutines.runBlocking

            fun main() {
                runBlocking {
                    println("Hello")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom runBlocking function in a suspend function passes`() {
        val code = """
            fun <T> runBlocking(block: () -> T): T = block()

            suspend fun main() {
                runBlocking {
                    println("Hello")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
