package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class EmptyRangeTest {

    private val environment = createEnvironment()

    private val sut = EmptyRange(Config.empty)

    @Test
    fun `a rangeTo range with the start greater than the end is reported`() {
        val code = """
            fun foo() {
                for (i in 9..0) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a char range with the start greater than the end is reported`() {
        val code = """
            val range = 'b'..'a'
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a double range with the start greater than the end is reported`() {
        val code = """
            val range = 1.0..0.0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a downTo range with the start less than the end is reported`() {
        val code = """
            fun test() {
                0 downTo 10
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until range with a negative end is reported`() {
        val code = """
            fun test() {
                0 until -1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until range with equal bounds is reported`() {
        val code = """
            fun test() {
                0 until 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a rangeUntil range with equal float bounds is reported`() {
        val code = """
            fun test() {
                0f..<0f
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unsigned range of val references with the start greater than the end is reported`() {
        val code = """
            val a: UShort = 1u
            val b: UShort = 0u
            val range = a..b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unsigned long range with the start greater than the end is reported`() {
        val code = """
            val range = 1UL..0UL
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a rangeTo call with the start greater than the end is reported`() {
        val code = """
            val range = 2.rangeTo(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a downTo range with equal bounds passes`() {
        val code = """
            fun test() {
                0 downTo 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a downTo range to a parameter passes`() {
        val code = """
            fun test(a: Int = 0) {
                1 downTo a
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a range of var references passes`() {
        val code = """
            fun test() {
                var start = 0
                var end = start
                end = 1
                for (i in start until end) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a range to a property with a custom getter passes`() {
        val code = """
            var x = 0
            val end: Int
                get() = x

            fun test() {
                val start = 0
                x = 1
                for (i in start until end) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unsigned range from the minimum to the maximum value passes`() {
        val code = """
            val range = UInt.MIN_VALUE..UInt.MAX_VALUE
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unsigned short range with a large end passes`() {
        val code = """
            val a: UShort = 100u
            val b: UShort = 50_000u
            val range = a..b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
