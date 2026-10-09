package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceSubstringWithIndexingOperationTest {

    private val environment = createEnvironment()

    private val sut = ReplaceSubstringWithIndexingOperation(Config.empty)

    @Test
    fun `a substring from zero to one is reported`() {
        val code = """
            fun foo() {
                "abc".substring(0, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from one to two is reported`() {
        val code = """
            fun foo() {
                "abc".substring(1, 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring of a function call result with adjacent indices is reported`() {
        val code = """
            fun bar(): String = "abc"

            fun foo(): String = bar().substring(2, 3)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a substring from zero to ten passes`() {
        val code = """
            fun foo() {
                "abc".substring(0, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring with variable indices passes`() {
        val code = """
            fun foo(s: String, i: Int): String = s.substring(i, i + 1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring with one index passes`() {
        val code = """
            fun foo(s: String): String = s.substring(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a substring member of a custom class passes`() {
        val code = $$"""
            class Text {
                fun substring(start: Int, end: Int): String = "$start$end"
            }

            fun foo(t: Text) {
                t.substring(0, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
