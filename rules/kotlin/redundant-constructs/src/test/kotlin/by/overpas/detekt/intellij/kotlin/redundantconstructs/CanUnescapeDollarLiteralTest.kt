package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CanUnescapeDollarLiteralTest {

    private val environment = createEnvironment()

    private val sut = CanUnescapeDollarLiteral(Config.empty)

    @Test
    fun `a slash escaped dollar at the end of a string is reported`() {
        val code = $$$$$$$"""
            fun test() = "\$\$\$"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `block escaped dollars at the end of a string are reported`() {
        val code = $$$$$$$"""
            fun test() = "${'$'}${'$'}${'$'}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `block escaped dollars before a number are reported`() {
        val code = $$$$$$$"""
            fun test() = "${'$'}$\$${'$'}${42 + 15}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `block escaped dollars in a raw string are reported`() {
        val code = $$$$$$$"""
            fun test() = '''${'$'}${'$'}${'$'}'''
        """.trimIndent().replace("'''", "\"\"\"")

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `escaped dollars in a prefixed string are reported`() {
        val code = $$$$$$$"""
            fun test() = $$"$${'$'}$${'$'}$${'$'}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `escaped dollars before a number in a prefixed string are reported`() {
        val code = $$$$$$$"""
            fun test() = $$"\$\$\$\$1"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe escaped dollar with a four dollar prefix is reported`() {
        val code = $$$$$$$"""
            fun test() = $$$$"\$$$$$${'$'}Foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a replaceable dollar after an unreplaceable one is reported`() {
        val code = $$$$$$$"""
            fun test() = '''hello ${'$'}{url} ${'$'}'''
        """.trimIndent().replace("'''", "\"\"\"")

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a slash escaped dollar before a letter passes`() {
        val code = $$$$$$$"""
            fun test() = "Foo\$Bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a block escaped dollar before a letter passes`() {
        val code = $$$$$$$"""
            fun test() = "Foo${'$'}Bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `backslashes in a raw string pass`() {
        val code = $$$$$$$"""
            fun test() = '''\$\$\$'''
        """.trimIndent().replace("'''", "\"\"\"")

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an escaped dollar after a dollar before an identifier in a prefixed string passes`() {
        val code = $$$$$$$"""
            fun test() = $$"\$$Foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an escaped dollar after a dollar before a brace in a prefixed string passes`() {
        val code = $$$$$$$"""
            fun test() = $$"\$${Foo}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unsafe escaped dollar with a four dollar prefix passes`() {
        val code = $$$$$$$"""
            fun test() = $$$$"\$$$$Foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
