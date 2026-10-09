package by.overpas.detekt.intellij.kotlin.codemigration

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class InfixCallToOrdinaryTest {

    private val environment = createEnvironment()

    private val sut = InfixCallToOrdinary(Config.empty)

    @Test
    fun `an infix call of a user function is reported`() {
        val code = """
            infix fun Int.combine(other: Int) = this + other

            fun foo(x: Int) = x combine 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call of a standard library function is reported`() {
        val code = """
            fun foo(x: Int) = (x shl 1).unaryMinus()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call with a lambda argument is reported`() {
        val code = """
            interface Foo {
                infix fun foo(f: (Int) -> Unit)
            }

            fun foo(x: Foo) {
                x foo { println(it * 2) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call on a not-null asserted receiver is reported`() {
        val code = """
            infix fun String.add(other: String) = this + other

            fun foo(x: String?) = x!! add "1"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an infix call with a parenthesized argument is reported`() {
        val code = """
            infix fun String.add(other: String) = this + other

            fun foo(x: String) = x add ("1" + "2")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `nested infix calls are reported`() {
        val code = """
            fun foo() = 1 to 2 to 3
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an operator binary expression passes`() {
        val code = """
            fun foo(x: String) = x == x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an ordinary call of an infix function passes`() {
        val code = """
            fun foo(x: Int) = x.shl(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
