package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantInterpolationPrefixTest {

    private val environment = createEnvironment()

    private val sut = RedundantInterpolationPrefix(Config.empty)

    @Test
    fun `a single dollar prefix of a plain string is reported`() {
        val code = """
            fun test() = $"sample text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single dollar prefix of an interpolated string is reported`() {
        val code = $$$"""
            fun test() = $"sample ${3 + 2} text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double dollar prefix of a plain string is reported`() {
        val code = """
            fun test() = $$"sample text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double dollar prefix of a string with non-interpolating dollars is reported`() {
        val code = """
            fun test() = $$"sample $$ text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a long prefix of a plain string is reported`() {
        val code = """
            fun test() = $$$$$$$$"sample text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double dollar prefix of an interpolated string passes`() {
        val code = $$$"""
            fun test() = $$"sample $${3 + 2} text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a double dollar prefix of a string with an escaped dollar passes`() {
        val code = $$$"""
            fun test() = $$"foo\\$bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a string without a prefix passes`() {
        val code = """
            fun test() = "sample text"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
