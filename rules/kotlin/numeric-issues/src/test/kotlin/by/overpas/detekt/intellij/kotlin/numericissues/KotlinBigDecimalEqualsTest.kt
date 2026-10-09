package by.overpas.detekt.intellij.kotlin.numericissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinBigDecimalEqualsTest {

    private val environment = createEnvironment()

    private val sut = KotlinBigDecimalEquals(Config.empty)

    @Test
    fun `an equality of two big decimals is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo() = BigDecimal(1.0) == BigDecimal(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an inequality of two big decimals is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo() = BigDecimal(1.0) != BigDecimal(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between big decimals is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo() = BigDecimal(1.0).equals(BigDecimal(1))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality with a nullable left big decimal is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal?) = decimal == BigDecimal(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality with a nullable right big decimal is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal?) = BigDecimal(1) == decimal
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality of two nullable big decimals is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal?, other: BigDecimal?) = decimal == other
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call with a nullable argument is reported`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal, other: BigDecimal?) = decimal.equals(other)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality of a big decimal and a nullable any passes`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal, other: Any?) = decimal == other
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality of big integers passes`() {
        val code = """
            import java.math.BigInteger

            fun foo() = BigInteger.ONE == BigInteger.TWO
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compareTo call between big decimals passes`() {
        val code = """
            import java.math.BigDecimal

            fun foo() = BigDecimal(1.0).compareTo(BigDecimal(1)) == 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an identity check of big decimals passes`() {
        val code = """
            import java.math.BigDecimal

            fun foo(decimal: BigDecimal, other: BigDecimal) = decimal === other
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
