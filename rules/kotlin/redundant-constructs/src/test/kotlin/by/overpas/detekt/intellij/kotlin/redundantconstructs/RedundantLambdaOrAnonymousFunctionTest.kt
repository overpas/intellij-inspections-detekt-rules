package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantLambdaOrAnonymousFunctionTest {

    private val sut = RedundantLambdaOrAnonymousFunction(Config.empty)

    @Test
    fun `an empty lambda that is called at once is reported`() {
        val code = """
            fun test() {
                {}()
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda in parentheses that is called at once is reported`() {
        val code = """
            fun test() {
                (((({}))))()
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda with parameters that is called at once is reported`() {
        val code = """
            val xx = ({ x: Int -> x + 1 })(42)
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda called with invoke is reported`() {
        val code = """
            val xx = { x: Int, y: Int -> x + y }.invoke(1, 2)
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda with a trailing lambda argument is reported`() {
        val code = """
            class Chain

            fun complicate(chain: Chain) {
                val vra = { chain: Chain, fn: Chain.() -> Chain ->
                    chain.fn()
                }(chain) { this.also { println(it) } }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an anonymous function that is called at once is reported`() {
        val code = """
            fun test() {
                (fun() {})()
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an anonymous function called with invoke is reported`() {
        val code = """
            val xx = (fun(x: Int, y: Int) = x + y).invoke(1, 2)
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an anonymous extension function that is called at once is reported`() {
        val code = """
            val xx = 1.(fun Int.(x: Int, y: Int) = this + x + y)(2, 3)
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lambda passed as an argument passes`() {
        val code = """
            val xx = with({ x: Int, y: Int -> x + y }) {
                invoke(1, 2)
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda in a variable passes`() {
        val code = """
            fun test() {
                val anonFun = { x: Int, y: Int -> x + y }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an anonymous function passed as an argument passes`() {
        val code = """
            val xx = with(fun(x: Int, y: Int) = x + y) {
                invoke(1, 2)
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an anonymous function in a variable passes`() {
        val code = """
            fun test() {
                val anonFun = fun() {}
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a named function passes`() {
        val code = """
            fun test() {
                fun local() {}
                local()
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
