package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceRangeStartEndInclusiveWithFirstLastTest {

    private val environment = createEnvironment()

    private val sut = ReplaceRangeStartEndInclusiveWithFirstLast(Config.empty)

    @Test
    fun `the start of an IntRange is reported`() {
        val code = """
            fun foo(range: IntRange) = range.start
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the endInclusive of an IntRange is reported`() {
        val code = """
            fun foo(range: IntRange) = range.endInclusive
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the start of a CharRange is reported`() {
        val code = """
            fun foo() {
                val range: CharRange = 'a'..'z'
                println(range.start)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the endInclusive of a LongRange is reported`() {
        val code = """
            fun foo() {
                val range: LongRange = 1L..2L
                println(range.endInclusive)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the bounds of unsigned ranges are reported`() {
        val code = """
            fun foo(ints: UIntRange, longs: ULongRange) = ints.start to longs.endInclusive
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `the start of a range literal is reported`() {
        val code = """
            fun foo() = (1..10).start
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the bounds of a custom range pass`() {
        val code = """
            class MyRange : ClosedRange<String> {
                override val start: String get() = "a"
                override val endInclusive: String = "z"
            }

            fun foo() = MyRange().endInclusive + MyRange().start
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the bounds of a ClosedRange pass`() {
        val code = """
            fun foo(range: ClosedRange<Int>) = range.start + range.endInclusive
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the bounds of a floating point range pass`() {
        val code = """
            fun foo() = (1.0..2.0).start
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `first and last of an IntRange pass`() {
        val code = """
            fun foo(range: IntRange) = range.first + range.last
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a start property of another class passes`() {
        val code = """
            class Interval(val start: Int)

            fun foo(interval: Interval) = interval.start
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
