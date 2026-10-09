package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSubstringWithSubstringAfterTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSubstringWithSubstringAfter(Config.empty)

    @Test
    fun `a substring from the index of a char is reported`() {
        val code = """
            fun foo(s: String) {
                s.substring(s.indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from the index of a string is reported`() {
        val code = """
            fun foo(s: String): String = s.substring(s.indexOf("ab"))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of an immutable property from its index of a char is reported`() {
        val code = """
            class A(val x: String)

            fun foo(a: A) {
                a.x.substring(a.x.indexOf('x'))
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
                a.bar().substring(a.bar().indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from the index of a char in another string passes`() {
        val code = """
            fun foo(s: String, t: String) {
                s.substring(t.indexOf('x'))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from an index with a start index passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(s.indexOf('x', 1))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring between two indices passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(s.indexOf('x'), s.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
