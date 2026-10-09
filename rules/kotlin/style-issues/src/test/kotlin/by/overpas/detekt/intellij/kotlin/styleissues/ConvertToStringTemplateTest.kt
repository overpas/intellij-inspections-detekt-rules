package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertToStringTemplateTest {

    private val environment = createEnvironment()

    private val sut = ConvertToStringTemplate(Config.empty)

    @Test
    fun `a literal plus a parameter plus a literal is reported`() {
        val code = """
            fun foo(p: Int) = "(" + p + ")"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a value followed by a literal that starts with a dot is reported`() {
        val code = """
            val x = "abc"
            val y = x + ".bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `special chars in char literals are reported`() {
        val code = """
            fun foo(p1: Int, p2: Int, p3: Int) = "a" + p1 + '\n' + p2 + '\r' + p3 + '\t'
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `existing templates that stay simple are reported`() {
        val code = $$"""
            val x = "abc"
            val y = "cde"
            val z = "$y" + "$x.bar"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a value followed by a char literal is reported`() {
        val code = """
            fun test(p: String) = p + '\''
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal followed by a value is reported`() {
        val code = """
            val x = "abc"
            val y = "d" + x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal followed by this is reported`() {
        val code = """
            class A {
                val s = "a: " + this
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toString call on a value is reported`() {
        val code = """
            fun test(a: Any) = "a: " + a.toString()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `only the outermost concatenation is reported`() {
        val code = """
            fun foo(a: Int, b: Int) = "(" + a + ", " + b + ")"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a value followed by a literal that starts with a letter passes`() {
        val code = """
            val x = "abc"
            val y = x + "postfix"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation of two values passes`() {
        val code = """
            val a = "abc"
            val b = "bcd"
            val x = a + b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation with a function call passes`() {
        val code = """
            fun bar() = 1
            val x = "abc" + bar()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation of literals and constants only passes`() {
        val code = """
            val x = "abc" + 1 + 2 + 'a'
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an addition of numbers passes`() {
        val code = """
            val x = 23 + 76
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom plus operator that returns a String passes`() {
        val code = $$"""
            class Foo(private val bar: Int) {
                operator fun plus(tail: String): String = "$bar.$tail"
            }

            val fooWithTail = Foo(10) + "tail"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation over several lines passes`() {
        val code = """
            fun foo(p: Int) = "(" +
                p + ")"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus assignment of a String passes`() {
        val code = """
            fun foo(p: Int) {
                var x = "abcd"
                x += p
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
