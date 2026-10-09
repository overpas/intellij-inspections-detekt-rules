package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class FloatingPointLiteralPrecisionTest {

    private val environment = createEnvironment()

    private val sut = FloatingPointLiteralPrecision(Config.empty)

    @Test
    fun `a double literal with excess digits is reported`() {
        val code = """
            val x = 9_999_999_999.000001
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double literal that rounds to one is reported`() {
        val code = """
            val x = 1.0000000000000001
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double literal in an argument is reported`() {
        val code = """
            fun foo(x: Double): Double = x * 2.0

            val z = foo(1.9999999999999999)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a large double literal with a fraction is reported`() {
        val code = """
            val x = 9999999999999999.1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a float literal with excess digits is reported`() {
        val code = """
            fun test(): Float {
                return 0.123456789f
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a float literal with an uppercase suffix is reported`() {
        val code = """
            fun test(): Float {
                return 0.123456789F
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a large integral float literal is reported`() {
        val code = """
            val x = 9_999_999_999f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a float literal with seven digits of pi is reported`() {
        val code = """
            val pi = 3.1415926f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double literal with eleven digits of pi passes`() {
        val code = """
            val pi = 3.14159265359
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a double literal with a short fraction passes`() {
        val code = """
            val x = 0.1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a double literal with trailing zeros passes`() {
        val code = """
            val x: Double = 0.100
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a double literal that is exactly representable as the shortest form passes`() {
        val code = """
            val x = 1.0000000000000002
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a float literal with six digits of pi passes`() {
        val code = """
            val pi = 3.141593f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a float literal with trailing zeros passes`() {
        val code = """
            val x = 0.100000000000000000000f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
