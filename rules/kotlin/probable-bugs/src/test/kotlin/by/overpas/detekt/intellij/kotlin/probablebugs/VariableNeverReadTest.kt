package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableNeverReadTest {

    private val environment = createEnvironment()

    private val sut = VariableNeverRead(Config.empty)

    @Test
    fun `a variable that is only assigned is reported`() {
        val code = """
            fun foo() {
                var x = 1
                x = 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable without an initializer that is only assigned is reported`() {
        val code = """
            fun foo() {
                var x: Int
                x = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable that is assigned in a loop and never read is reported`() {
        val code = """
            fun foo(items: List<Int>) {
                var last = 0
                for (item in items) {
                    last = item
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable that is assigned and read passes`() {
        val code = """
            fun foo(): Int {
                var x = 1
                x = 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable that is never used at all passes`() {
        val code = """
            fun foo() {
                val x = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable that is read in a lambda passes`() {
        val code = """
            fun foo(): () -> Int {
                var x = 1
                x = 2
                return { x }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property that is only assigned passes`() {
        val code = """
            class Foo {
                var x = 1

                fun bar() {
                    x = 2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable that is incremented passes`() {
        val code = """
            fun foo() {
                var count = 0
                count++
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
