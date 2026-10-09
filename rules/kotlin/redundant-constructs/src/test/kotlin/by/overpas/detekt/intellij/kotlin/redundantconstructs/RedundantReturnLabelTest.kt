package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantReturnLabelTest {

    private val environment = createEnvironment()

    private val sut = RedundantReturnLabel(Config.empty)

    @Test
    fun `a labeled return without a value in a named function is reported`() {
        val code = """
            fun test() {
                return@test
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return with a value in a named function is reported`() {
        val code = """
            fun test(s: String?): Int {
                if (s != null) {
                    return@test 1
                }
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return in an anonymous function that targets this function is reported`() {
        val code = """
            fun foo(f: (String?) -> Int) {}

            fun test() {
                foo(fun(it: String?): Int {
                    if (it != null) return@foo 1
                    return 0
                })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return in a lambda passes`() {
        val code = """
            fun foo(f: (String?) -> Int) {}

            fun test() {
                foo {
                    if (it != null) return@foo 1
                    0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled return from a lambda in a named function passes`() {
        val code = """
            fun test(list: List<Int>) {
                list.forEach {
                    if (it == 0) return@forEach
                    println(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return without a label passes`() {
        val code = """
            fun test(s: String?): Int {
                if (s != null) return 1
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
