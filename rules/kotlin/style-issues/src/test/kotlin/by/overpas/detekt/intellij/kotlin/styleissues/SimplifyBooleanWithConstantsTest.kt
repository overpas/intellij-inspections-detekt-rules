package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyBooleanWithConstantsTest {

    private val environment = createEnvironment()

    private val sut = SimplifyBooleanWithConstants(Config.empty)

    @Test
    fun `a chain of boolean literals is reported once`() {
        val code = """
            fun foo(): Boolean = true && false || true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `comparisons of a boolean with a literal are reported`() {
        val code = """
            fun use(arg: Boolean): List<Boolean> = listOf(arg == true, false == arg, arg != false, true != arg)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `a disjunction of literal comparisons is reported once`() {
        val code = """
            fun foo(arg: Boolean): Boolean = arg == true || arg == false
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal inside parentheses is reported once`() {
        val code = """
            fun foo(y: Boolean, z: Boolean): Boolean = (y && false) || (z && (y || true))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constant comparison of numbers is reported`() {
        val code = """
            fun foo(y: Boolean): Boolean = 2 > 1 && y || y
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a literal next to function calls is reported`() {
        val code = """
            fun bar(): Boolean = false

            fun foo(y: Boolean): Boolean = true && (bar()) && y
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constant argument of assert is reported`() {
        val code = """
            fun foo() {
                assert(true || false)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an annotated operand compared with a literal is reported`() {
        val code = """
            var b = true

            @Target(AnnotationTarget.EXPRESSION)
            @Retention(AnnotationRetention.SOURCE)
            annotation class Ann

            fun foo(): Boolean = @Ann b == true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized constant expression compared with a nullable boolean is reported`() {
        val code = """
            fun foo(y: Boolean, n: Boolean?): Boolean = (true && y) == n
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `only the literal comparisons of non-null booleans are reported`() {
        val code = """
            data class Test(val notnull: Boolean, val nullable: Boolean?)

            fun test(a: Test, b: Test?): Boolean =
                a.notnull == true || a.nullable == true || b?.notnull == true || b?.nullable == true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `nullable booleans compared with literals pass`() {
        val code = """
            fun foo(arg: Boolean?): List<Boolean> = listOf(arg == true, arg == true || arg == false)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a platform boolean compared with a literal passes`() {
        val code = """
            fun bar(s: String): Boolean = java.lang.Boolean.valueOf(s) == true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an expression without constants passes`() {
        val code = """
            fun foo(y: Boolean): Boolean = y && y || y
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `local values that are not constants pass`() {
        val code = """
            fun foo(): List<Boolean> {
                val x = true
                val z = false
                val a = 4
                val b = 5
                return listOf(x && z, a < b, a == b || a != b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comparison of positive and negative zero passes`() {
        val code = """
            fun foo(): List<Boolean> = listOf(-0.0 == +0.0, +0.0f == -0.0f)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comparison of equal zeros is reported`() {
        val code = """
            fun foo(): Boolean = +0.0f == +0.0f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }
}
