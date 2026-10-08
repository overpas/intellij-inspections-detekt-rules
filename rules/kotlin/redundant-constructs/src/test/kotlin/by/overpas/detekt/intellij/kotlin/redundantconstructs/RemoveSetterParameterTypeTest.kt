package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveSetterParameterTypeTest {

    private val sut = RemoveSetterParameterType(Config.empty)

    @Test
    fun `an explicit setter parameter type is reported`() {
        val code = """
            var x: String = ""
                set(param: String) {
                    field = "${'$'}param "
                }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit setter parameter type in a class is reported`() {
        val code = """
            class My {
                var y: Int = 1
                    set(param: Int) {
                        field = param - 1
                    }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a setter parameter without a type passes`() {
        val code = """
            class My {
                var w: Boolean = true
                    set(param) {
                        field = !param
                    }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a private setter passes`() {
        val code = """
            class My {
                var z: Double = 3.14
                    private set
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function parameter type passes`() {
        val code = """
            fun foo(param: String) = param
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
