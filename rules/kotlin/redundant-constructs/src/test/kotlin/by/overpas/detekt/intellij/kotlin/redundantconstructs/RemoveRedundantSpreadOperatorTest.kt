package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveRedundantSpreadOperatorTest {

    private val environment = createEnvironment()

    private val sut = RemoveRedundantSpreadOperator(Config.empty)

    @Test
    fun `a spread of arrayOf is reported`() {
        val code = """
            fun foo(vararg x: String) {}

            fun bar() {
                foo(*arrayOf("abc"))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of a primitive array factory is reported`() {
        val code = """
            fun foo(vararg x: Int) {}

            fun bar() {
                foo(*intArrayOf(1))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of an empty array is reported`() {
        val code = """
            fun foo(vararg x: String) {}

            fun bar() {
                foo(*emptyArray<String>())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of arrayOf with other arguments is reported`() {
        val code = """
            fun foo(vararg x: String) {}

            fun bar() {
                foo(*arrayOf("abc", "def"), "ghi", "jkl")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of arrayOf that holds a spread is reported`() {
        val code = """
            fun foo(vararg xs: String) {}

            fun bar(ys: Array<String>) {
                foo(*arrayOf(*ys, "zzz"))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of a collection literal in an annotation is reported`() {
        val code = """
            annotation class Ann(vararg val value: String)

            @Ann(*["a", "b"])
            class Test
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread that keeps the same overload without it is reported`() {
        val code = """
            fun execute(x: Int, y: Int) = "foo"

            fun execute(vararg xs: Int) = "bar"

            fun main() {
                execute(*intArrayOf(5))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread of arrayOf in a qualified call is reported`() {
        val code = """
            class A {
                fun foo(vararg x: String) {}
            }

            fun bar(a: A) {
                a.foo(*arrayOf("abc"))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a spread that selects another overload without it passes`() {
        val code = """
            fun execute(x: Int) = "foo"

            fun execute(vararg xs: Int) = "bar"

            fun main() {
                execute(*intArrayOf(5))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a spread of an empty array that selects another overload without it passes`() {
        val code = """
            fun foo(vararg args: Int) {}

            fun foo() {}

            fun test() {
                foo(*intArrayOf())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a spread of an empty array with an ambiguous overload passes`() {
        val code = """
            fun foo(vararg args: Int) {}

            fun foo(vararg args: Double) {}

            fun test() {
                foo(*intArrayOf())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a named spread argument passes`() {
        val code = """
            fun foo(vararg x: String) {}

            fun bar() {
                foo(x = *arrayOf("abc"))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a spread of an array variable passes`() {
        val code = """
            fun foo(vararg x: String) {}

            fun bar(array: Array<String>) {
                foo(*array)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
