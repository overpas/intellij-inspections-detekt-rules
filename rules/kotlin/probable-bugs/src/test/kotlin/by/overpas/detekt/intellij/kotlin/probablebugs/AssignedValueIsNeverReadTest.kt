package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class AssignedValueIsNeverReadTest {

    private val environment = createEnvironment()

    private val sut = AssignedValueIsNeverRead(Config.empty)

    @Test
    fun `an assignment that is never read is reported`() {
        val code = """
            fun foo(): Int {
                var x = 1
                val y = x
                x = 2
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assignment that is overwritten before a read is reported`() {
        val code = """
            fun foo(): Int {
                var x: Int
                x = 1
                x = 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a compound assignment that is never read is reported`() {
        val code = """
            fun foo(a: Int): Int {
                var x = a
                val y = x
                x += 1
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an increment that is never read is reported`() {
        val code = """
            fun foo(a: Int): Int {
                var x = a
                val y = x
                x++
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assignment that is read later passes`() {
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
    fun `an assignment that is read in the next loop iteration passes`() {
        val code = """
            fun foo(): Int {
                var sum = 0
                for (i in 1..5) {
                    sum = sum + i
                }
                return sum
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assignment to a property passes`() {
        val code = """
            class A {
                var x = 1

                fun foo() {
                    x = 2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
