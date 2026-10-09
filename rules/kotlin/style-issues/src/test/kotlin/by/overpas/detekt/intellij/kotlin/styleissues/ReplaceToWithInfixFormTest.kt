package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceToWithInfixFormTest {

    private val environment = createEnvironment()

    private val sut = ReplaceToWithInfixForm(Config.empty)

    @Test
    fun `a dot call of the standard to function is reported`() {
        val code = """
            class A
            class B

            fun foo(a: A, b: B) {
                val pair = a.to(b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a dot call of to on a literal is reported`() {
        val code = """
            val pair = 1.to("one")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a dot call of a custom infix to function is reported`() {
        val code = """
            class A {
                infix fun to(x: Int): Int = x
            }

            fun foo() {
                A().to(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a dot call of a non-infix to function passes`() {
        val code = """
            class A {
                fun to(x: Int) {
                }
            }

            fun foo() {
                A().to(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a to call with explicit type arguments passes`() {
        val code = """
            class Receiver(val x: Int = 0)
            class Argument(val y: Int = 1)

            val test = "".to<String, Receiver.(Argument) -> Unit> {
                println(x)
                println(it.y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an infix to call passes`() {
        val code = """
            val pair = 1 to "one"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe call of to passes`() {
        val code = """
            fun foo(a: String?) = a?.to(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
