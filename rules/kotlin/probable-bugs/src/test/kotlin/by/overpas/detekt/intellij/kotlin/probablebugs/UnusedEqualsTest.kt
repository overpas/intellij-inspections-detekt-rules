package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedEqualsTest {

    private val environment = createEnvironment()

    private val sut = UnusedEquals(Config.empty)

    @Test
    fun `an unused equality statement is reported`() {
        val code = """
            fun foo(a: Int, b: Int) {
                a == b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused equals call is reported`() {
        val code = """
            fun foo(a: Int, b: Int) {
                a.equals(b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `unused equality statements in a Unit lambda are reported`() {
        val code = """
            fun foo3(d: () -> Unit) {}

            fun foo(a: Int, b: Int) {
                foo3 {
                    a == b
                    a == b
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a non-last equality statement in a Boolean lambda is reported`() {
        val code = """
            fun foo2(c: () -> Boolean) {}

            fun foo(a: Int, b: Int) {
                foo2 {
                    a == b
                    a == b
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused equality in an if branch is reported`() {
        val code = """
            fun foo(a: Int) {
                if (a > 1) a == 10 else a == -1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an unused equality in a local function is reported`() {
        val code = """
            fun foo(a: Int) {
                fun bar() {
                    a == 1
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality assigned to a variable passes`() {
        val code = """
            fun foo(a: Int, b: Int) {
                val e = a == b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned equality passes`() {
        val code = """
            fun foo(a: Int): Boolean {
                return a == 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality in an expression body passes`() {
        val code = """
            fun foo(a: Int) = a == 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality used as a condition passes`() {
        val code = """
            fun foo(a: Int, b: Int) {
                if (a == b) return
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality in a used if expression passes`() {
        val code = """
            fun foo(a: Int) {
                val eq = if (a > 1) a == 10 else a == -1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality returned from a run lambda passes`() {
        val code = """
            fun foo(a: Int, b: Int) {
                run {
                    a == b
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality returned from a predicate lambda passes`() {
        val code = """
            class Test(val successCondition: (Int) -> Boolean)

            fun foo() = Test({ it == 69 })
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
