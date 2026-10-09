package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSubstringWithTakeTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSubstringWithTake(Config.empty)

    @Test
    fun `a substring from zero to a constant is reported`() {
        val code = """
            fun foo(s: String) {
                s.substring(0, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to a variable is reported`() {
        val code = """
            fun foo(s: String, n: Int): String = s.substring(0, n)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring followed by a call on a CharSequence receiver is reported`() {
        val code = """
            fun CharSequence.lastPart(delimiter: Char): Char = this[lastIndexOf(delimiter) + 1]

            fun foo(s: String) {
                s.substring(0, 10).
                    lastPart(',')
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of a string literal followed by a String call is reported`() {
        val code = """
            fun foo() {
                "s".substring(0, 10).
                    substringAfterLast(',')
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of a CharSequence followed by a String call passes`() {
        val code = """
            fun foo(s: CharSequence) {
                s.substring(0, 10).substringAfterLast(',')
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from a constant property passes`() {
        val code = """
            const val x = 0

            fun foo(s: String) {
                s.substring(x, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from an expression that evaluates to zero passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(1 - 1, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring from a non-zero index passes`() {
        val code = """
            fun foo(s: String) {
                s.substring(1, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring to the length minus n of the same string passes`() {
        val code = """
            fun foo(s: String, suffix: String): String? =
                if (s.endsWith(suffix = suffix)) s.substring(0, s.length - suffix.length) else null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring of a function call receiver passes`() {
        val code = """
            fun bar(): String = "abc"

            fun foo() {
                bar().substring(0, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
