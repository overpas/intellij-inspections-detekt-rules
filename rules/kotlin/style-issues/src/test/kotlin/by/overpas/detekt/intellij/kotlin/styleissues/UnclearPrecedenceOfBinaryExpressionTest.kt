package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnclearPrecedenceOfBinaryExpressionTest {

    private val environment = createEnvironment()

    private val sut = UnclearPrecedenceOfBinaryExpression(Config.empty)

    @Test
    fun `an elvis inside an equality check is reported`() {
        val code = """
            fun foo(a: Int?, b: Int?, c: Int?) = a ?: b == c
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an elvis inside an is check is reported`() {
        val code = """
            fun foo(a: Boolean?, b: Any) = a ?: b is Int
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an addition inside an elvis is reported`() {
        val code = """
            fun foo(a: Int?) = a ?: 1 + 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an arithmetic chain inside an elvis is reported once`() {
        val code = """
            fun foo(a: Int?) = a ?: 1 + 2 * 4
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call inside an elvis is reported`() {
        val code = """
            fun foo(a: Int?) = a ?: 1 xor 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call inside an elvis with a parenthesized left side is reported`() {
        val code = """
            fun foo() = (if (true) 1 else null) ?: 1 xor 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a disjunction in a when guard is reported`() {
        val code = """
            fun test(a: Any) {
                when (a) {
                    is String if a[0] == 'f' || a[0] == 'b' -> Unit
                    else -> Unit
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `disjunctions in two when guards are reported`() {
        val code = """
            fun test(param: Any) {
                when (param) {
                    is Int if param < 0 || param > 10 -> println("foo")
                    is Double if param < 0.0 || param > 10.0 -> println("baz")
                    else -> println("bar")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a chain of elvis operators passes`() {
        val code = """
            fun foo(i: Int?, j: Int?, k: Int?) = i ?: j ?: k
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a cast inside an elvis passes`() {
        val code = """
            fun foo(i: Int?, j: Any?) = i ?: j as Int?
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an arithmetic expression without an elvis passes`() {
        val code = """
            fun foo() = 1 + 2 * 4
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parenthesized arithmetic expression inside an elvis passes`() {
        val code = """
            fun foo(a: Int?) = a ?: (1 + 2 * 4)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parenthesized elvis inside an addition passes`() {
        val code = """
            fun foo(i: Int?) = (i ?: 0) + 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis on the right side of an assignment passes`() {
        val code = """
            fun test(i: Int?): Int {
                val y: Int
                y = i ?: 1
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an addition inside a range passes`() {
        val code = """
            fun foo() = 1 + 2..4
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a conjunction in a when guard passes`() {
        val code = """
            fun test(param: Any, flag: Boolean) {
                when (param) {
                    is Int if param < 0 && flag -> println("foo")
                    else -> println("bar")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parenthesized disjunction in a when guard passes`() {
        val code = """
            fun test(param: Any, flag: Boolean) {
                when (param) {
                    is Int if (param < 0 || param > 10) && flag -> println("foo")
                    else -> println("bar")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a disjunction in a when branch without a guard passes`() {
        val code = """
            fun test(param: Any, flag1: Boolean, flag2: Boolean) {
                when {
                    (param is Int) && flag1 || flag2 -> println("foo")
                    else -> println("bar")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
