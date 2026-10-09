package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CanConvertToMultiDollarStringTest {

    private val environment = createEnvironment()

    private val sut = CanConvertToMultiDollarString(Config.empty)

    @Test
    fun `an escaped dollar is reported`() {
        val code = """
            fun test() = "\$"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a dollar char block entry is reported`() {
        val code = $$"""
            fun test() = "${'$'}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a dollar string block entry is reported`() {
        val code = $$"""
            fun test() = "${"$"}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `four escaped dollars before an identifier are reported`() {
        val code = $$$"""
            fun test() = "${'$'}\$\$${"$"}Foo ${'$'}\$\$${"$"}Bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `many escaped dollars before a non-identifier are reported`() {
        val code = $$$"""
            fun test() = "\$\$\$${'$'}${'$'}${"$"}${"$"} not a dollar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `dollar block entries in a raw string are reported`() {
        val code = $$$"""
            fun test() = ""$$${'"'}${'$'}${'$'}${'$'}Foo""$$${'"'}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `five escaped dollars before an identifier pass`() {
        val code = $$"""
            fun test() = "\$\$\$\$\$Foo"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `five escaped dollars before a block pass`() {
        val code = $$"""
            fun test() = "\$\$\$\$\${Foo}"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `five escaped dollars before a backtick identifier pass`() {
        val code = $$"""
            fun test() = "\$\$\$\$\$`Foo`"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a second sequence of five escaped dollars passes`() {
        val code = $$"""
            fun test() = "\$\$Foo \$\$\$\$\$_Bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `five dollar block entries in a raw string pass`() {
        val code = $$$"""
            fun test() = ""$$${'"'}${'$'}${'$'}${'$'}${'$'}${'$'}Foo""$$${'"'}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a string without escaped dollars passes`() {
        val code = $$$"""
            fun test(some: Int) = "$10 10$ $$$$$ ${3 + 2} $some"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a prefixed string passes`() {
        val code = """
            fun test() = $$"\$"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
