package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantLambdaArrowTest {

    private val environment = createEnvironment()

    private val sut = RedundantLambdaArrow(Config.empty)

    @Test
    fun `an arrow without parameters is reported`() {
        val code = """
            fun foo(f: () -> Unit) {}

            fun bar() {
                foo { -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit it parameter is reported`() {
        val code = """
            fun foo(f: (String) -> Unit) {}

            fun bar() {
                foo { it ->
                    print(it)
                    print(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit it parameter of a forEach lambda is reported`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).forEach { it -> println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit it parameter of a safe call lambda is reported`() {
        val code = """
            fun test(list: List<String>?) = list?.map { it -> it.length }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an arrow without parameters for a named argument is reported when overloads stay unambiguous`() {
        val code = """
            fun bar(f: () -> Unit) {}
            fun bar(g: (Int, Int) -> Unit) {}

            fun test() {
                bar(f = { -> })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit it parameter in a vararg array is reported`() {
        val code = """
            fun main() {
                registerHandler(handlers = arrayOf(
                    { _ -> },
                    { it -> },
                ))
            }

            fun registerHandler(vararg handlers: (String) -> Unit) {
                handlers.forEach { it.invoke("hello") }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an underscore parameter passes`() {
        val code = """
            fun foo(f: (String) -> Unit) {}

            fun bar() {
                foo { _ -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a named parameter passes`() {
        val code = """
            fun foo(f: (Int) -> Unit) {}

            fun bar() {
                foo { i -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an it parameter with an explicit type passes`() {
        val code = """
            fun takeP(a: Any) {}

            val z = takeP { it: Int -> it.div(2) }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an arrow that selects an overload passes`() {
        val code = """
            fun bar(f: () -> Unit) {}
            fun bar(f: (Int) -> Unit) {}

            fun test() {
                bar { -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an arrow in a parenthesized argument that selects an overload passes`() {
        val code = """
            fun bar(f: () -> Unit) {}
            fun bar(f: (Int) -> Unit) {}

            fun test() {
                bar(f = { -> })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an explicit it parameter that selects an overload passes`() {
        val code = """
            fun bar(f: () -> Unit) {}
            fun bar(f: (Int) -> Unit) {}

            fun test() {
                bar { it -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an arrow of a lambda in an if branch passes`() {
        val code = """
            fun test(flag: Boolean): () -> Int {
                return if (flag) { -> 42 } else { -> 0 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an arrow of a lambda that uses an outer it passes`() {
        val code = """
            fun run(f: () -> Int) = f()

            fun test() {
                listOf("A").forEach {
                    run { -> it.length }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
