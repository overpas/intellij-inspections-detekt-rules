package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedLambdaExpressionBodyTest {

    private val environment = createEnvironment()

    private val sut = UnusedLambdaExpressionBody(Config.empty)

    @Test
    fun `an unused call of a function with a lambda body is reported`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar() = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused call inside an if statement is reported`() {
        val code = """
            fun foo(flag: Boolean) {
                if (flag) {
                    bar()
                }
            }

            fun bar() = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused call of a function with a lambda with parameters is reported`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar() = { i: Int ->
                println(i)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused call of a method with a lambda body is reported`() {
        val code = """
            class Foo {
                fun bar() = { println() }
            }

            fun foo(foo: Foo) {
                foo.bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a call whose result is stored passes`() {
        val code = """
            fun foo() {
                val a = bar()
                a()
            }

            fun bar() = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call whose result is compared passes`() {
        val code = """
            fun foo() {
                if (bar() != null) {
                    println()
                }
            }

            fun bar() = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call whose result is invoked passes`() {
        val code = """
            fun foo() {
                bar()()
            }

            fun bar() = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call in an enum entry argument passes`() {
        val code = """
            enum class Test(f: () -> Unit) {
                A(getFunc()),
            }

            fun getFunc(): () -> Unit = {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused call of a function with a block body that returns a lambda passes`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar(): () -> Unit {
                return {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused call of a function with a lambda body and no function type passes`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar(): Any = {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
