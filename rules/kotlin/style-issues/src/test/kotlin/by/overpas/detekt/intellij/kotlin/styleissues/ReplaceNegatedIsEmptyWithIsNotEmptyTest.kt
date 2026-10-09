package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceNegatedIsEmptyWithIsNotEmptyTest {

    private val environment = createEnvironment()

    private val sut = ReplaceNegatedIsEmptyWithIsNotEmpty(Config.empty)

    @Test
    fun `a negated isEmpty call on a list is reported`() {
        val code = """
            fun test(): Boolean {
                val list = listOf(1, 2, 3)
                return !list.isEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated parenthesized isEmpty call is reported`() {
        val code = """
            fun test(): Boolean {
                val list = listOf(1, 2, 3)
                return !((list.isEmpty()))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isBlank call with an implicit receiver is reported`() {
        val code = """
            fun String.test(): Boolean {
                return !isBlank()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isEmpty call on a string is reported`() {
        val code = """
            fun test(s: String) = !s.isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isNotEmpty call on a string is reported`() {
        val code = """
            fun test(s: String) = !s.isNotEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isBlank call on a string is reported`() {
        val code = """
            fun test(s: String) = !s.isBlank()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isNotBlank call on a string is reported`() {
        val code = """
            fun test(s: String) = !s.isNotBlank()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated isEmpty call on a mutable map is reported`() {
        val code = """
            fun test(map: MutableMap<String, Int>) = !map.isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an isEmpty call without negation passes`() {
        val code = """
            fun test(s: String) = s.isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an isBlank call without negation passes`() {
        val code = """
            fun test(s: String) = s.isBlank()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated isEmpty function of another class passes`() {
        val code = """
            class Box {
                fun isEmpty(): Boolean = true
            }

            fun test(box: Box) = !box.isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
