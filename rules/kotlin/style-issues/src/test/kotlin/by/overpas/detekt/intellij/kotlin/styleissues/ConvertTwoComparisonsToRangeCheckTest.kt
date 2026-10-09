package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertTwoComparisonsToRangeCheckTest {

    private val environment = createEnvironment()

    private val sut = ConvertTwoComparisonsToRangeCheck(Config.empty)

    @Test
    fun `two inclusive comparisons of an int are reported`() {
        val code = """
            fun foo(bar: Int) = 0 <= bar && bar <= 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two strict comparisons in flipped order are reported`() {
        val code = """
            fun foo(arg: Int) = 6 > arg && arg > 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons with hexadecimal constants are reported`() {
        val code = """
            fun test(c: Int) = c > 0xd800 && c < 0xdc00
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two strict comparisons of a char are reported`() {
        val code = """
            fun foo(bar: Char) = bar > 'a' && 'z' > bar
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons with non-constant bounds are reported`() {
        val code = """
            fun foo(bar: Int, min: Int, max: Int) = min < bar && bar < max
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons joined with or are reported`() {
        val code = """
            fun foo(bar: Int) = bar < 0 || bar > 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons of a double with int constants are reported`() {
        val code = """
            fun foo(bar: Double) = 0 <= bar && bar <= 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons of a custom comparable are reported`() {
        val code = """
            class A(val value: Int) : Comparable<A> {
                override fun compareTo(other: A) = value.compareTo(other.value)
            }

            val low = A(0)
            val high = A(100)

            fun test(a: A) = low <= a && a <= high
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons of a platform type are reported`() {
        val code = """
            import java.time.LocalDate

            fun test(target: LocalDate, from: LocalDate): Boolean {
                val to = from.plusDays(1)
                return from <= target && target <= to
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons inside a contains operator that delegates to a standard range are reported`() {
        val code = """
            class Bounds(val low: Int, val high: Int) {
                operator fun contains(x: Int): Boolean = low <= x && x < high
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two comparisons of an int with double constants pass`() {
        val code = """
            fun foo(bar: Int) = bar >= 0.0 && 10.0 >= bar
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exclusive lower bound of a double passes`() {
        val code = """
            fun foo(bar: Double) = bar > 0 && 10 >= bar
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `two comparisons of a type that is not comparable pass`() {
        val code = """
            class A(val value: Int) {
                operator fun compareTo(other: A) = value.compareTo(other.value)
            }

            val low = A(0)
            val high = A(100)

            fun test(a: A) = low <= a && a <= high
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `bounds with side effects pass`() {
        val code = """
            var x = 42

            fun foo(arg: Int) = arg <= ++x && --x <= arg
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `two comparisons of different values pass`() {
        val code = """
            fun foo(a: Int, b: Int) = a > 0 && b < 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `two comparisons inside the contains operator of the range pass`() {
        val code = """
            class TimeIndex(val intValue: Int) : Comparable<TimeIndex> {
                override fun compareTo(other: TimeIndex): Int = intValue.compareTo(other.intValue)

                operator fun rangeTo(other: TimeIndex): TimeIndexRange = TimeIndexRange(this, other)
            }

            data class TimeIndexRange(val start: TimeIndex, val end: TimeIndex) {
                operator fun contains(index: TimeIndex): Boolean = start <= index && index <= end
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
