package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantConditionIfTest {

    private val environment = createEnvironment()

    private val sut = ConstantConditionIf(Config.empty)

    @Test
    fun `an if statement with a true condition is reported`() {
        val code = """
            fun baz(s: String) {}

            fun bar() {
                if (true) {
                    baz("a")
                    baz("b")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if statement with a false condition is reported`() {
        val code = """
            fun foo(x: Int) {}

            fun bar() {
                if (false) {
                    foo(1)
                    foo(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if expression with a true condition is reported`() {
        val code = """
            fun foo(x: Int) {}

            fun bar() {
                foo(if (true) {
                    foo(1)
                    1
                } else 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized constant condition is reported`() {
        val code = """
            fun bar(): Int = if ((false)) 1 else 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else if with a constant condition is reported`() {
        val code = """
            fun foo(s: String) {}

            fun bar(s: String?) {
                if (s == null) {
                    foo("a")
                } else if (true) {
                    foo("b")
                } else {
                    foo("c")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if with a const val condition passes`() {
        val code = """
            const val TRUE = true

            fun bar(): Int = if (TRUE) 1 else 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a constant expression condition passes`() {
        val code = """
            const val TRUE = true

            fun bar(): Int = if (false && TRUE) 1 else 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a comparison of constants passes`() {
        val code = """
            const val s = "debug"

            fun main() {
                if (s == "debug") {
                    println(s)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
