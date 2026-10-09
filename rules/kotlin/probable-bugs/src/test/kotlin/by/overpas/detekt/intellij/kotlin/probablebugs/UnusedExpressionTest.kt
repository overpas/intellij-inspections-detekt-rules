package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedExpressionTest {

    private val environment = createEnvironment()

    private val sut = UnusedExpression(Config.empty)

    @Test
    fun `an unused constant statement is reported`() {
        val code = """
            fun foo() {
                42
                println()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused this reference is reported`() {
        val code = """
            class C {
                fun foo() {
                    this
                    println()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused variable reference is reported`() {
        val code = """
            fun foo(a: Int) {
                a
                println()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused string literal is reported`() {
        val code = """
            fun foo() {
                "text"
                println()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused operator call passes`() {
        val code = """
            fun foo(a: Int, b: Int) {
                a + b
                println()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused last statement of a Unit function is reported`() {
        val code = """
            fun foo(a: Int) {
                println()
                a
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a used expression passes`() {
        val code = """
            fun foo(a: Int, b: Int): Int {
                val sum = a + b
                return sum
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the last expression of a lambda passes`() {
        val code = """
            fun foo(a: Int) = run {
                println()
                a
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function call statement passes`() {
        val code = """
            fun bar(): Int = 42

            fun foo() {
                bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
