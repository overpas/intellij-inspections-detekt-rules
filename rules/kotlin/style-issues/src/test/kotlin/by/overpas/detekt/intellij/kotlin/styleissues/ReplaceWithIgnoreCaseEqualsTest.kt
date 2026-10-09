package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithIgnoreCaseEqualsTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithIgnoreCaseEquals(Config.empty)

    @Test
    fun `a comparison of lowercase results is reported`() {
        val code = """
            fun test(a: String, b: String): Boolean {
                return a.lowercase() == b.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison of uppercase results is reported`() {
        val code = """
            fun test(a: String, b: String): Boolean {
                return a.uppercase() == b.uppercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison with a nullable left operand is reported`() {
        val code = """
            fun test(a: String?, b: String): Boolean {
                return a?.lowercase() == b.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison with a nullable right operand is reported`() {
        val code = """
            fun test(a: String, b: String?): Boolean {
                return a.lowercase() == b?.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison with two nullable operands is reported`() {
        val code = """
            fun test(a: String?, b: String?): Boolean {
                return a?.uppercase() == b?.uppercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison with an implicit receiver on the left is reported`() {
        val code = """
            fun String.test(s: String): Boolean {
                return lowercase() == s.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison with an implicit receiver on the right is reported`() {
        val code = """
            fun String.test(s: String): Boolean {
                return s.lowercase() == lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-equal comparison passes`() {
        val code = """
            fun test(a: String, b: String): Boolean {
                return a.lowercase() != b.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comparison with one converted operand passes`() {
        val code = """
            fun test(a: String, b: String): Boolean {
                return a == b.lowercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comparison of different conversions passes`() {
        val code = """
            fun test(a: String, b: String): Boolean {
                return a.lowercase() == b.uppercase()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comparison of custom lowercase functions passes`() {
        val code = """
            fun String.lowercase(times: Int): String = repeat(times)

            fun test(a: String, b: String): Boolean {
                return a.lowercase(2) == b.lowercase(2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
