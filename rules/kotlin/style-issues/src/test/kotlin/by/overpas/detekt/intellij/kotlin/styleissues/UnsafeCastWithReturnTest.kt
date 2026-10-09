package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsafeCastWithReturnTest {

    private val environment = createEnvironment()

    private val sut = UnsafeCastWithReturn(Config.empty)

    @Test
    fun `an unsafe cast with return in a variable initializer is reported`() {
        val code = """
            fun test(x: Any): Any? {
                val y = x as String ?: return null
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unsafe cast with return in a call argument is reported`() {
        val code = """
            fun test(x: Any): Any? {
                val y = foo(x as String ?: return null)
                return y
            }

            fun foo(x: String?): Any? {
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unsafe cast with a parenthesized return is reported`() {
        val code = """
            fun test(x: Any) {
                val y = x as String ?: (return)
                println(y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unsafe cast with return in a local var is reported`() {
        val code = """
            fun test(x: Any) {
                var y = x as String ?: return
                y += "a"
                println(y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe cast with return passes`() {
        val code = """
            fun test(x: Any): Any? {
                val y = x as? String ?: return null
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unsafe cast with a default value passes`() {
        val code = """
            fun test(x: Any?): Any? {
                val y = x as String? ?: "default"
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unsafe cast with return in a return statement passes`() {
        val code = """
            fun test(x: Any): Any? {
                return x as String ?: return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an elvis with return without a cast passes`() {
        val code = """
            fun test(x: String?): Any? {
                val y = x ?: return null
                return y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
