package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SafeCastWithReturnTest {

    private val environment = createEnvironment()

    private val sut = SafeCastWithReturn(Config.empty)

    @Test
    fun `a safe cast with return is reported`() {
        val code = """
            fun test(x: Any): Int? {
                x as? String ?: return null
                return foo(x)
            }

            fun foo(x: String) = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized safe cast with return is reported`() {
        val code = """
            fun test(x: Any): Int? {
                (x as? String) ?: (return null)
                return foo(x)
            }

            fun foo(x: String) = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe cast with labeled return is reported`() {
        val code = """
            fun test(list: List<Any>) {
                list.mapNotNull {
                    it as? String ?: return@mapNotNull null
                    foo(it)
                }
            }

            fun foo(x: String) = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cast with return is reported`() {
        val code = """
            fun foo(o: Any) {
                o as String ?: return
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe cast with return as the last statement of an unused lambda is reported`() {
        val code = """
            fun test(x: Any) {
                run {
                    x as? String ?: return
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe cast with labeled return as the last statement of an unused lambda is reported`() {
        val code = """
            fun test(x: Any) {
                run {
                    x as? String ?: return@run
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe cast with return as the last statement of a used lambda passes`() {
        val code = """
            fun test(x: Any) {
                val s = run {
                    x as? String ?: return
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe cast with return used as an initializer passes`() {
        val code = """
            fun test(x: Any): Int? {
                val s = x as? String ?: return null
                return foo(s)
            }

            fun foo(x: String) = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe cast with a default value passes`() {
        val code = """
            fun test(x: Any): String {
                return x as? String ?: ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
