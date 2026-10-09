package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSubstringWithDropLastTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSubstringWithDropLast(Config.empty)

    @Test
    fun `a substring from zero to length minus n of a parameter is reported`() {
        val code = """
            fun foo(s: String) {
                s.substring(0, s.length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to length minus n of an immutable property is reported`() {
        val code = """
            class A(val x: String)

            fun foo(a: A) {
                a.x.substring(0, a.x.length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to length minus n of a local value is reported`() {
        val code = """
            fun foo(): String {
                val s = "abcdef"
                return s.substring(0, s.length - 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to length minus a variable expression is reported`() {
        val code = """
            fun foo(s: String, suffix: String): String = s.substring(0, s.length - suffix.length)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of a function call receiver passes`() {
        val code = """
            class A {
                fun bar(): String = "abc"
            }

            fun foo(a: A) {
                a.bar().substring(0, a.bar().length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring of a mutable property passes`() {
        val code = """
            class A(var x: String)

            fun foo(a: A) {
                a.x.substring(0, a.x.length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from a non-zero index passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(3, s.length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring to the length minus n of another string passes`() {
        val code = """
            fun foo(s: String, t: String) {
                s.substring(0, t.length - 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring to the length plus n passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(0, s.length + 0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring member of a custom class passes`() {
        val code = $$"""
            class Text(val length: Int) {
                fun substring(start: Int, end: Int): String = "$start$end"
            }

            fun foo(t: Text) {
                t.substring(0, t.length - 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
