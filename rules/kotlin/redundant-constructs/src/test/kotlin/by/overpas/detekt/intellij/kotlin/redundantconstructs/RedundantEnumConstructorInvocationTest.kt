package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantEnumConstructorInvocationTest {

    private val environment = createEnvironment()

    private val sut = RedundantEnumConstructorInvocation(Config.empty)

    @Test
    fun `an empty constructor invocation of an enum entry is reported`() {
        val code = """
            enum class Foo {
                A(),
                B(),
                C,
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an empty constructor invocation with a default argument is reported`() {
        val code = """
            enum class Baz(i: Int = 0) {
                A(1),
                B(),
                C(),
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an empty constructor invocation of an enum entry with a body is reported`() {
        val code = """
            enum class Foo {
                A() {
                    override fun toString() = "a"
                },
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an enum entry with arguments passes`() {
        val code = """
            enum class Bar(i: Int) {
                A(1),
                B(2),
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an enum entry without parentheses passes`() {
        val code = """
            enum class Foo {
                A,
                B,
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
