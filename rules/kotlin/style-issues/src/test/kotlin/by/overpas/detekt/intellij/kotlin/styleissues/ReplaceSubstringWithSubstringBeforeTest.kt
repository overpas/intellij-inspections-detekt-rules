package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSubstringWithSubstringBeforeTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSubstringWithSubstringBefore(Config.empty)

    @Test
    fun `a substring from zero to the index of a char is reported`() {
        val code = """
            fun foo(s: String) {
                s.substring(0, s.indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to the index of a string is reported`() {
        val code = """
            fun foo(s: String): String = s.substring(0, s.indexOf("ab"))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of an immutable property to its index of a char is reported`() {
        val code = """
            class A(val x: String)

            fun foo(a: A) {
                a.x.substring(0, a.x.indexOf('x'))
            }
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
                a.bar().substring(0, a.bar().indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from a non-zero index passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(1, s.indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring to the index of a char in another string passes`() {
        val code = """
            fun foo(s: String, t: String) {
                s.substring(0, t.indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring to an index with ignore case passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(0, s.indexOf('x', ignoreCase = true))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
