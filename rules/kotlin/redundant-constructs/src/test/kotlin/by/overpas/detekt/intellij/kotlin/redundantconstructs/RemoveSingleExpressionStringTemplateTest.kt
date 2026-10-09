package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveSingleExpressionStringTemplateTest {

    private val environment = createEnvironment()

    private val sut = RemoveSingleExpressionStringTemplate(Config.empty)

    @Test
    fun `a template with a single String variable is reported`() {
        val code = $$"""
            val foo = "foo"
            val bar = "$foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a template with a single String call is reported`() {
        val code = $$"""
            val bar = "${1.hashCode().toString()}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a template with a single non-String expression passes`() {
        val code = $$"""
            val bar = "${1.hashCode()}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a template with a single nullable String expression passes`() {
        val code = $$"""
            class Info(val number: String?)

            fun instance(info: Info) = "${info.number}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an empty string passes`() {
        val code = """
            val bar = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plain string passes`() {
        val code = """
            val bar = "Hello"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a template with two expressions passes`() {
        val code = $$"""
            val foo = "foo"
            val bar = "$foo$foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a template with text and an expression passes`() {
        val code = $$"""
            val foo = "foo"
            val bar = "text$foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
