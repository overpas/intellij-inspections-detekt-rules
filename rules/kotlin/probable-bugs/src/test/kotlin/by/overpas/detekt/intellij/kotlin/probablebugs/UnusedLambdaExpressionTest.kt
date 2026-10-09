package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedLambdaExpressionTest {

    private val environment = createEnvironment()

    private val sut = UnusedLambdaExpression(Config.empty)

    @Test
    fun `a lambda statement in a function body is reported`() {
        val code = """
            fun foo() {
                { println() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda statement in a branch of an if statement is reported`() {
        val code = """
            fun foo(flag: Boolean) {
                if (flag) {
                    { println() }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `two lambda statements are reported`() {
        val code = """
            fun foo() {
                { println(1) }
                { println(2) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a lambda stored in a variable passes`() {
        val code = """
            fun foo() {
                val action = { println() }
                action()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda that is invoked passes`() {
        val code = """
            fun foo() {
                { println() }()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda passed to run passes`() {
        val code = """
            fun foo() {
                run { println() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda returned from a function passes`() {
        val code = """
            fun foo() = { println() }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda returned as the result of another lambda passes`() {
        val code = """
            val factory = { { println() } }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
