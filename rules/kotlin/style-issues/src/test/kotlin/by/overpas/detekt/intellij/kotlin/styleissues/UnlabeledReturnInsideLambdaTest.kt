package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnlabeledReturnInsideLambdaTest {

    private val environment = createEnvironment()

    private val sut = UnlabeledReturnInsideLambda(Config.empty)

    @Test
    fun `an unlabeled return inside an inline lambda is reported`() {
        val code = """
            inline fun foo(f: () -> Unit) {}

            fun test(): Int {
                foo {
                    return 0
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled return inside a lambda of a backticked function is reported`() {
        val code = """
            inline fun foo(f: () -> Unit) {}

            fun `T _ T`(): Int {
                foo {
                    return 0
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled return inside a forEach lambda is reported`() {
        val code = """
            fun test(list: List<Int>) {
                list.forEach {
                    if (it == 0) return
                    println(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `unlabeled returns inside nested inline lambdas are reported`() {
        val code = """
            fun test(list: List<List<Int>>) {
                list.forEach { inner ->
                    if (inner.isEmpty()) return
                    inner.forEach {
                        if (it == 0) return
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a labeled return inside a lambda passes`() {
        val code = """
            inline fun foo(f: () -> Unit) {}

            fun test(): Int {
                foo {
                    return@test 0
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return to the lambda label passes`() {
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
    fun `a return inside a local function within a lambda passes`() {
        val code = """
            inline fun foo(f: () -> Unit) {}

            fun test(): Int {
                foo {
                    fun bar() {
                        return
                    }
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return inside an anonymous function within a lambda passes`() {
        val code = """
            fun test(list: List<Int>) {
                list.forEach {
                    listOf(it).forEach(fun(x: Int) {
                        if (x == 0) return
                    })
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return directly in a function body passes`() {
        val code = """
            fun test(x: Int): Int {
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unlabeled return inside a lambda in a property getter passes`() {
        val code = """
            val x: Int
                get() {
                    run {
                        return 1
                    }
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unlabeled return inside a non-inline lambda passes`() {
        val code = """
            fun foo(f: () -> Unit) {}

            fun test(): Int {
                foo {
                    return 0
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, allowCompilationErrors = true)

        assertEquals(0, findings.size)
    }
}
