package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertFromMultiDollarToRegularStringTest {

    private val environment = createEnvironment()

    private val sut = ConvertFromMultiDollarToRegularString(Config.empty)

    @Test
    fun `a prefixed string without interpolation is reported`() {
        val code = """
            fun test() = $$"foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a prefixed string with a simple name entry is reported`() {
        val code = $$$"""
            fun test(a: Any) = $$"$$a"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a prefixed string with dollar sequences is reported`() {
        val code = $$$"""
            fun test() = $$"foo$ $$ $$$ bar$baz"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a prefixed string with escape sequences is reported`() {
        val code = $$$"""
            fun test() = $$"\$ \$foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a triple dollar prefixed string is reported`() {
        val code = $$$$"""
            fun test(x: Any) = $$$"$$$x$a$$${x}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a prefixed raw string with a block entry is reported`() {
        val code = $$$"""
            fun test(a: Int, b: Int) = $$""$$${'"'}$${a + b}""$$${'"'}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a string without a prefix passes`() {
        val code = $$$"""
            fun test(a: Int, b: Int) = "$${a + b}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a raw string without a prefix passes`() {
        val code = $$$"""
            fun test(a: Int) = ""$$${'"'}$$a""$$${'"'}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
