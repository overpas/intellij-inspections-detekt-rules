package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceToStringWithStringTemplateTest {

    private val environment = createEnvironment()

    private val sut = ReplaceToStringWithStringTemplate(Config.empty)

    @Test
    fun `a toString call on a local variable is reported`() {
        val code = """
            fun test(): String {
                val x = 1
                return x.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toString call on a parameter is reported`() {
        val code = """
            fun test(x: Any): String = x.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toString call on a call result is reported`() {
        val code = """
            data class Num(val x: Int)

            fun demo(x: Int) = Num(x).toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toString call on a nullable reference is reported`() {
        val code = """
            fun test(x: Any?): String = x.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toString call on a literal passes`() {
        val code = """
            fun test(): String = 1.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a toString call inside a string template passes`() {
        val code = $$"""
            fun test(): String {
                val x = 1
                return "Foo: ${x.toString()}"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a toString call with an argument passes`() {
        val code = """
            fun test(x: Int): String = x.toString(16)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe toString call passes`() {
        val code = """
            fun test(x: Any?): String? = x?.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a toString call on a qualified expression passes`() {
        val code = """
            fun test(s: String): String = s.length.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a string template passes`() {
        val code = $$"""
            fun test(x: Int): String = "$x"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
