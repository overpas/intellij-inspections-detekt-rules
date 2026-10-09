package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyNegatedBinaryExpressionTest {

    private val environment = createEnvironment()

    private val sut = SimplifyNegatedBinaryExpression(Config.empty)

    @Test
    fun `a negated equality check is reported`() {
        val code = """
            fun test(a: Int, b: Int): Boolean = !(a == b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated inequality check is reported`() {
        val code = """
            fun test(a: Int, b: Int): Boolean = !(a != b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `negated integer comparisons are reported`() {
        val code = """
            fun test(a: Int, b: Int): List<Boolean> = listOf(!(a < b), !(a <= b), !(a > b), !(a >= b))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `negated type checks are reported`() {
        val code = """
            fun test(a: Any): List<Boolean> = listOf(!(a is Int), !(a !is String))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `negated in checks are reported`() {
        val code = """
            class A(val e: Int) {
                operator fun contains(i: Int): Boolean = e == i
            }

            fun test(a: A): List<Boolean> = listOf(!(0 in a), !(0 !in a))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `negated boolean literals are reported`() {
        val code = """
            fun test(): List<Boolean> = listOf(!true, !false)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a double negation passes`() {
        val code = """
            fun test(): Boolean = !(!true)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated infix function call passes`() {
        val code = """
            infix fun Int.lt(b: Int): Boolean = this < b

            fun test(): Boolean = !(1 lt 2)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated floating-point comparison passes`() {
        val code = """
            fun test(x: Double, y: Float): List<Boolean> = listOf(!(x < 1.0), !(1f >= y))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated variable passes`() {
        val code = """
            fun test(flag: Boolean): Boolean = !flag
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
