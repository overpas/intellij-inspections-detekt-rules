package by.overpas.detekt.intellij.kotlin.styleissues.convertsealedclasstosealedinterface

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertSealedClassToSealedInterfaceTest {

    private val environment = createEnvironment()

    private val sut = ConvertSealedClassToSealedInterface(Config.empty)

    @Test
    fun `a sealed class with an abstract method is reported`() {
        val code = """
            sealed class Result {
                abstract fun process()
            }

            class Success : Result() {
                override fun process() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with an abstract property is reported`() {
        val code = """
            sealed class Result {
                abstract val value: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with a custom getter and setter is reported`() {
        val code = """
            sealed class Result {
                var value: String
                    get() = "default"
                    set(v) {
                        println(v)
                    }
            }

            class Success : Result()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with an open and a private method is reported`() {
        val code = """
            sealed class Result {
                open fun process() = println("default")

                private fun helper() = println("private")
            }

            class Success : Result() {
                override fun process() = println("success")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with synchronized companion functions is reported`() {
        val code = """
            sealed class Task {
                companion object {
                    @Synchronized
                    fun run() {
                        println("JB")
                    }
                }
            }

            class A : Task()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a generic sealed class with object and nested inheritors is reported`() {
        val code = """
            sealed class Result<out T : Any>() {
                class Nested : Result<Nothing>()
            }

            class Success<T : Any>(val result: T) : Result<T>()

            object Loading : Result<Nothing>()

            data object Empty : Result<Nothing>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class whose inheritor delegates another interface is reported`() {
        val code = """
            interface Other {
                fun other()
            }

            class OtherImpl : Other {
                override fun other() {}
            }

            sealed class Result

            class Success(other: OtherImpl) : Result(), Other by other
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with inheritors that have secondary constructors is reported`() {
        val code = """
            sealed class Result

            class Failure : Result {
                constructor(message: String) : super()
                constructor(code: Int) : super()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface passes`() {
        val code = """
            sealed interface Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a constructor parameter passes`() {
        val code = """
            sealed class Result<out T : Any>(val long: Long)

            class Success<T : Any>(val result: T, long: Long) : Result<T>(long)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with an annotated constructor passes`() {
        val code = """
            sealed class Result @Deprecated("test") constructor()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a secondary constructor passes`() {
        val code = """
            sealed class Result {
                constructor(code: Int)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a property initializer passes`() {
        val code = """
            sealed class Result {
                val long: Long = System.currentTimeMillis()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a lateinit property passes`() {
        val code = """
            sealed class Result {
                lateinit var data: String
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with an init block passes`() {
        val code = """
            sealed class Result {
                init {
                    println("init")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a final override passes`() {
        val code = """
            interface Base {
                fun process()
            }

            sealed class Result : Base {
                final override fun process() = println("done")
            }

            class Success : Result()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a synchronized method passes`() {
        val code = """
            sealed class Task {
                @Deprecated("old")
                @Synchronized
                fun run() {}
            }

            class A : Task()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a private inline method is reported`() {
        val code = """
            sealed class Either {
                private inline fun <X> run(block: () -> X): X = block()

                fun describe(): String = run { "either" }

                object Left : Either()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class with a public inline method passes`() {
        val code = """
            sealed class Either<out F, out S> {
                inline fun <X> fold(onFailure: (F) -> X, onSuccess: (S) -> X): X =
                    when (this) {
                        is Success -> onSuccess(value)
                        is Failure -> onFailure(value)
                    }

                data class Success<out S>(val value: S) : Either<Nothing, S>()

                data class Failure<out F>(val value: F) : Either<F, Nothing>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with an inline property getter passes`() {
        val code = """
            sealed class Shape {
                val sides: Int
                    inline get() = 0

                object Circle : Shape()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a tailrec method passes`() {
        val code = """
            sealed class Counter {
                tailrec fun count(n: Int): Int = if (n == 0) 0 else count(n - 1)

                object Zero : Counter()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with an external method passes`() {
        val code = """
            sealed class Native {
                external fun call(): Int

                object Impl : Native()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a JvmOverloads method passes`() {
        val code = """
            sealed class Task {
                @JvmOverloads
                fun run(times: Int = 1) = times

                object Once : Task()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a JvmName property getter passes`() {
        val code = """
            sealed class Task {
                @get:JvmName("title")
                val name: String
                    get() = "task"

                object Once : Task()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a superclass passes`() {
        val code = """
            open class Base

            sealed class Result : Base()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a delegated supertype passes`() {
        val code = """
            interface Delegate {
                fun doWork()
            }

            sealed class Result(delegate: Delegate) : Delegate by delegate
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with an internal abstract property passes`() {
        val code = """
            sealed class S {
                internal abstract val x: Int
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed class with a protected abstract method passes`() {
        val code = """
            sealed class S {
                protected abstract fun process()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
