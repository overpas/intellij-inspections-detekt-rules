package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceUntilWithRangeUntilTest {

    private val environment = createEnvironment()

    private val sut = ReplaceUntilWithRangeUntil(Config.empty)

    @Test
    fun `an until call on Int values is reported`() {
        val code = """
            fun main() {
                0 until 10
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until call in a for loop is reported`() {
        val code = """
            fun main(n: Int) {
                for (i in 0 until n) {
                    println(i)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until call on Long values is reported`() {
        val code = """
            fun test(from: Long, to: Long) {
                from until to
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until call on Char values is reported`() {
        val code = """
            fun test(from: Char, to: Char) {
                from until to
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until call on Byte values is reported`() {
        val code = """
            fun test(from: Byte, to: Byte) {
                from until to
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an until call on UInt values is reported`() {
        val code = """
            fun test(from: UInt, to: UInt) {
                from until to
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a rangeUntil operator passes`() {
        val code = """
            fun main() {
                0..<10
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a dot call of until passes`() {
        val code = """
            fun main() {
                0.until(10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom until function passes`() {
        val code = """
            infix fun Int.until(other: String): Int = this + other.length

            fun main() {
                0 until "abc"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a downTo call passes`() {
        val code = """
            fun main() {
                10 downTo 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
