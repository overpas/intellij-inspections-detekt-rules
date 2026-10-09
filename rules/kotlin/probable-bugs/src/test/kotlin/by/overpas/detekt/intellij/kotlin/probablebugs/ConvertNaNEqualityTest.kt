package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertNaNEqualityTest {

    private val environment = createEnvironment()

    private val sut = ConvertNaNEquality(Config.empty)

    @Test
    fun `an equality check with Double NaN is reported`() {
        val code = """
            fun test() {
                val t = 5.0 == Double.NaN
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an inequality check with Float NaN is reported`() {
        val code = """
            fun test() {
                val x = 0.5f != Float.NaN
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an inequality check of a sum with Double NaN is reported`() {
        val code = """
            fun test() {
                val result = 5.0 + 3.0 != Double.NaN
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with an imported NaN on the left is reported`() {
        val code = """
            import kotlin.Double.Companion.NaN

            fun test() {
                val t = NaN == 5.0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with Java Double NaN is reported`() {
        val code = """
            fun test() {
                val t = java.lang.Double.NaN == 5.0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with Java Float NaN is reported`() {
        val code = """
            fun test() {
                val a = 0.5f
                val x = a == java.lang.Float.NaN
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with a user property named NaN passes`() {
        val code = """
            class A {
                val NaN = 0.3
            }

            fun test() {
                val a = A()
                val t = a.NaN == 0.5
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an isNaN call passes`() {
        val code = """
            fun test() {
                val t = 5.0.isNaN()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an identity check with Double NaN passes`() {
        val code = """
            fun test(value: Any) {
                val t = value === Double.NaN
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
