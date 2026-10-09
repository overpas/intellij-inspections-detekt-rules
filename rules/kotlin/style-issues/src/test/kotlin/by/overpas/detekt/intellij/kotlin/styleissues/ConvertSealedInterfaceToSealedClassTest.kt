package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertSealedInterfaceToSealedClassTest {

    private val environment = createEnvironment()

    private val sut = ConvertSealedInterfaceToSealedClass(Config.empty)

    @Test
    fun `a sealed interface with an abstract method is reported`() {
        val code = """
            sealed interface Result {
                fun process()
            }

            class Success : Result {
                override fun process() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface with an abstract property is reported`() {
        val code = """
            sealed interface Result {
                val value: String
            }

            class Success(override val value: String) : Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface with nested inheritors is reported`() {
        val code = """
            sealed interface Event {
                class Click : Event
                class Scroll : Event
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface with a default method is reported`() {
        val code = """
            sealed interface Result {
                fun process() = println("default")
            }

            class Success : Result {
                override fun process() = println("success")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a generic sealed interface with object inheritors is reported`() {
        val code = """
            sealed interface Result<out T : Any>

            class Success<T : Any>(val result: T) : Result<T>

            object Loading : Result<Nothing>

            data object Empty : Result<Nothing>
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface with inheritors through a type alias is reported`() {
        val code = """
            sealed interface Result

            typealias Res = Result

            class Success : Res

            object Loading : Res
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface whose inheritor has primary and secondary constructors is reported`() {
        val code = """
            sealed interface Result

            class Success(val data: String) : Result {
                constructor(data: String, source: String) : this(source + data)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed interface without inheritors is reported`() {
        val code = """
            sealed interface Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sealed class passes`() {
        val code = """
            sealed class Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plain interface passes`() {
        val code = """
            interface Result

            class Success : Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed interface with a delegating inheritor passes`() {
        val code = """
            sealed interface Result {
                fun process()
            }

            class Impl : Result {
                override fun process() {}
            }

            class Delegated(impl: Impl) : Result by impl
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed interface with an enum inheritor passes`() {
        val code = """
            sealed interface Result

            enum class Status : Result {
                SUCCESS,
                FAILURE,
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed interface with an interface inheritor passes`() {
        val code = """
            sealed interface Result

            interface SpecialResult : Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed interface whose inheritor has only secondary constructors passes`() {
        val code = """
            sealed interface Result

            class Failure : Result {
                constructor(message: String)
                constructor(code: Int)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sealed interface whose inheritor has a superclass passes`() {
        val code = """
            open class Base

            sealed interface Result

            class Success : Base(), Result
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
