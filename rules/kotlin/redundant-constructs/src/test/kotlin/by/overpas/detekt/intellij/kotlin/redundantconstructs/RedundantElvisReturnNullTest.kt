package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantElvisReturnNullTest {

    private val environment = createEnvironment()

    private val sut = RedundantElvisReturnNull(Config.empty)

    @Test
    fun `an elvis return null in a return is reported`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                return foo() ?: return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an elvis return null with a label on the outer return is reported`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                return@test foo() ?: return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an elvis labeled return null in a return is reported`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                return foo() ?: return@test null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an elvis return null in parentheses is reported`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                return (((foo() ?: return null)))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `another binary operator with a return null passes`() {
        val code = """
            operator fun Any?.div(other: Any): Any? = this

            fun test(s: Any?): Any? {
                return s / return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis return null outside of a return passes`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                val i = foo() ?: return null
                return i
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis return of a non-null value passes`() {
        val code = """
            fun foo(): Int? = null

            fun test(): Int? {
                return foo() ?: return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis return null to another target passes`() {
        val code = """
            inline fun <T> myRun(action: () -> T): T = action()

            fun test(param: String?): String? {
                val result: String = myRun {
                    return@myRun param ?: return null
                }
                return result
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis return null with a non-null left side passes`() {
        val code = """
            fun bar(): Int = 1

            fun test(): Int? {
                return bar() ?: return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
