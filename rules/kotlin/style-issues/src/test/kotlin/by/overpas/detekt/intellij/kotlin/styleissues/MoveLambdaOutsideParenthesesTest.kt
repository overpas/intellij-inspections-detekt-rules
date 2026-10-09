package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveLambdaOutsideParenthesesTest {

    private val environment = createEnvironment()

    private val sut = MoveLambdaOutsideParentheses(Config.empty)

    @Test
    fun `a lambda after another argument is reported`() {
        val code = """
            fun foo() {
                bar(2, { it })
            }

            fun bar(a: Int, b: (Int) -> Int) {
                b(a)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single lambda argument is reported`() {
        val code = """
            fun bar(f: () -> Unit) {}

            fun test() {
                bar({
                    val a = 1
                })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a multiline lambda after several arguments is reported`() {
        val code = """
            fun foo() {
                bar(3, 2, 1, {
                    val x = 3
                    it * x
                })
            }

            fun bar(name1: Int, name2: Int, name3: Int, name4: (Int) -> Int): Int = name4(name1) + name2 + name3
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled lambda is reported`() {
        val code = """
            fun foo() {
                bar(2, l@{ it })
            }

            fun bar(a: Int, b: (Int) -> Int) {
                b(a)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda passed to a functional value is reported`() {
        val code = """
            fun foo(p: (Int, () -> Int) -> Unit) {
                p(1, { 2 })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda for a suspend function type is reported`() {
        val code = """
            fun runSuspend(block: suspend () -> Unit) {}

            fun usage() {
                runSuspend({ println() })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda for a type parameter is reported`() {
        val code = """
            fun <T> foo(t: T) {}

            fun test() {
                foo({ "a" })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda for a functional interface is reported`() {
        val code = """
            fun interface Action {
                fun run()
            }

            fun perform(action: Action) {}

            fun test() {
                perform({ println() })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda passed to the result of a call is reported`() {
        val code = """
            fun bar() {
                foo { "one" }({ "two" })
            }

            fun foo(a: () -> String): (() -> String) -> Unit = { }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda outside parentheses passes`() {
        val code = """
            fun foo() {
                bar() { it }
            }

            fun bar(b: (Int) -> Int) {
                b(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda in parentheses with a trailing lambda passes`() {
        val code = """
            fun foo() {
                bar({ it }) { it }
            }

            fun bar(p1: (Int) -> Int, p2: (Int) -> Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda for a parameter that is not the last passes`() {
        val code = """
            fun test(a: (String) -> Unit = {}, b: (String) -> Unit = {}) {}

            fun foo() {
                test({ })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda followed by an optional parameter passes`() {
        val code = """
            fun foo() {
                bar({ it })
            }

            fun bar(b: (Int) -> Int, option: Int = 0) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda for a vararg parameter passes`() {
        val code = """
            fun foo(x: Int, vararg functions: () -> Unit) {}

            fun main() {
                foo(0, { })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a named lambda argument passes`() {
        val code = """
            fun foo() {
                bar(a = 2, b = { it })
            }

            fun bar(a: Int, b: (Int) -> Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `two lambda arguments pass`() {
        val code = """
            fun test(a: (String) -> Unit = {}, b: (String) -> Unit = {}) {}

            fun foo() {
                test({ }, { })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda in a delegated supertype passes`() {
        val code = """
            interface I

            class C1(s: String, f: (String) -> String) : I

            class C2 : I by C1("", { "" })
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
