package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class VariableInitializerIsRedundantTest {

    private val environment = createEnvironment()

    private val sut = VariableInitializerIsRedundant(Config.empty)

    @Test
    fun `an initializer overwritten before a read is reported`() {
        val code = """
            fun foo(): Int {
                var x = 1
                x = 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an initializer overwritten in both branches is reported`() {
        val code = """
            fun foo(flag: Boolean): String {
                var s = ""
                if (flag) {
                    s = "a"
                } else {
                    s = "b"
                }
                return s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an initializer with a call overwritten before a read is reported`() {
        val code = """
            fun bar() = 1

            fun foo(): Int {
                var x = bar()
                x = 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an initializer that is read passes`() {
        val code = """
            fun foo(): Int {
                var x = 1
                x += 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an initializer overwritten in one branch only passes`() {
        val code = """
            fun foo(flag: Boolean): Int {
                var x = 1
                if (flag) {
                    x = 2
                }
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable without an initializer passes`() {
        val code = """
            fun foo(): Int {
                val x: Int
                x = 2
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class property initializer passes`() {
        val code = """
            class C {
                var x = 1

                init {
                    x = 2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
