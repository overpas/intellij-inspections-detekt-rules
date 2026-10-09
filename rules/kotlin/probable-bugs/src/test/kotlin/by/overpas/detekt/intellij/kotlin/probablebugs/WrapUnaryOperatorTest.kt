package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class WrapUnaryOperatorTest {

    private val environment = createEnvironment()

    private val sut = WrapUnaryOperator(Config.empty)

    @Test
    fun `a minus before an integer literal with a call is reported`() {
        val code = """
            val x = -1.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus before a double literal with a call is reported`() {
        val code = """
            val x = -1.1.dec()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus before a float literal with a call is reported`() {
        val code = """
            val x = -1.1f.dec()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus before an integer literal with a call is reported`() {
        val code = """
            val x = +1.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus before a double literal with a call is reported`() {
        val code = """
            val x = +3.14.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus separated by a space from the literal with a call is reported`() {
        val code = """
            val x = 1 - - 1.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus before a variable with a call passes`() {
        val code = """
            val i = 100
            val x = -i.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a minus before a chain of calls on a literal passes`() {
        val code = """
            val x = 1 - -1.dec().dec()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a binary minus before a literal with a call passes`() {
        val code = """
            val x = 1 - 1.inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a minus before a literal without a call passes`() {
        val code = """
            val x = -1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negation of a boolean literal with a call passes`() {
        val code = """
            val x = !true.not()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a minus before a property of an object passes`() {
        val code = """
            object Constants {
                val ONE = 1
            }

            val x = -Constants.ONE
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a minus before a string literal with a call passes`() {
        val code = """
            val x = -"2".toInt()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a wrapped literal with a call passes`() {
        val code = """
            val x = (-1).inc()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
