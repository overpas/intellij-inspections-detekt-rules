package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertRangeCheckToTwoComparisonsTest {

    private val environment = createEnvironment()

    private val sut = ConvertRangeCheckToTwoComparisons(Config.empty)

    @Test
    fun `an in check of an Int range is reported`() {
        val code = """
            fun foo(bar: Int) = bar in 1..10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of a Double range is reported`() {
        val code = """
            fun foo(bar: Double) = bar in 1.0..10.0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of a range with variable bounds is reported`() {
        val code = """
            fun foo(bar: Int, min: Int, max: Int) = bar in min..max
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of a downTo progression is reported`() {
        val code = """
            fun foo(bar: Int) = bar in 10 downTo 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of an until range is reported`() {
        val code = """
            fun foo(bar: Int) = bar in 1 until 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of an open-ended range is reported`() {
        val code = """
            fun foo(bar: Int) = bar in 1..<10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an in check of a rangeTo call is reported`() {
        val code = """
            fun foo(bar: Int) = bar in 1.rangeTo(10)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not in check of a range is reported`() {
        val code = """
            fun foo(bar: Int) = bar !in 0..10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not in check of a downTo progression in an if condition is reported`() {
        val code = """
            fun foo(x: Int) {
                if (x !in 10 downTo 1) {
                    println("not in range")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a range check inside a for loop is reported`() {
        val code = """
            fun foo(bar: Int) {
                for (item in 1..10) {
                    println(bar in 1..10)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop over a range passes`() {
        val code = """
            fun foo() {
                for (bar in 1..2) {
                    println(bar)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a range check with operands of different types passes`() {
        val code = """
            val n: Number = 2.5

            fun foo() = n in 2..10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a check against an array passes`() {
        val code = """
            fun foo(bar: Int) = bar in arrayOf(1, 2, 3)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a range check with side effects passes`() {
        val code = """
            var x = 5
            var y = 10

            fun foo() = ++x in --x..y++
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a check of a user-defined range passes`() {
        val code = """
            class MyInt(val value: Int) {
                infix operator fun rangeTo(other: MyInt) = MyIntRange(this.value, other.value)
            }

            class MyIntRange(val start: Int, val end: Int) {
                operator fun contains(item: MyInt) = item.value in start..end
            }

            fun boo(bar: MyInt, min: MyInt, max: MyInt) = bar in min..max
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
