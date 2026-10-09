package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantValueArgumentTest {

    private val environment = createEnvironment()

    private val sut = RedundantValueArgument(Config.empty)

    @Test
    fun `a single argument equal to the default value is reported`() {
        val code = """
            fun foo(a: Int = 1) {}

            fun test() {
                foo(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `each argument equal to its default value is reported`() {
        val code = """
            fun foo(a: Int = 1, b: Int = 2) {}

            fun test() {
                foo(1, 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a constant argument equal to the default value is reported`() {
        val code = """
            fun foo(a: Int = 1, b: Int = 2) {}

            object Obj {
                const val CONSTANT = 2
            }

            fun test() {
                foo(3, Obj.CONSTANT)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constructor argument equal to the default value is reported`() {
        val code = """
            class A(val a: Int = 5)

            fun main() {
                val a = A(5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a named argument equal to the default value is reported`() {
        val code = """
            fun foo(a: Int = 1, b: Int = 2, c: Int) {}

            fun test() {
                foo(b = 3, c = 3, a = 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an argument equal to the default value of the base function is reported`() {
        val code = """
            open class A1 {
                open fun myFun(b: Boolean = false) {}
            }

            open class A2 : A1() {
                override fun myFun(b: Boolean) {}
            }

            class A3 : A2() {
                override fun myFun(b: Boolean) {}
            }

            fun test(a3: A3) {
                a3.myFun(false)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an argument with another value passes`() {
        val code = """
            fun foo(a: Int = 1, b: Int = 2) {}

            fun test() {
                foo(3, 3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable argument passes`() {
        val code = """
            fun foo(a: Int = 1, b: Int = 2) {}

            fun test(x: Int) {
                foo(3, x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an argument followed by vararg arguments passes`() {
        val code = """
            fun foo(a: Int = 1, vararg b: Int) {}

            fun test() {
                foo(1, 2, 3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an argument followed by a spread argument passes`() {
        val code = """
            fun foo(a: Int = 1, vararg b: Int) {}

            fun test() {
                foo(1, *intArrayOf(2, 3))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
