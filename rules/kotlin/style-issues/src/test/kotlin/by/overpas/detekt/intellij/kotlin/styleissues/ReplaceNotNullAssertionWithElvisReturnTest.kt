package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceNotNullAssertionWithElvisReturnTest {

    private val environment = createEnvironment()

    private val sut = ReplaceNotNullAssertionWithElvisReturn(Config.empty)

    @Test
    fun `a not-null assertion in a Unit function is reported`() {
        val code = """
            fun test(i: Int?) {
                val x = i!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion on a call result in a Unit function is reported`() {
        val code = """
            fun foo(): Int? = null

            fun test() {
                foo()!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion on a property in a function with a nullable return type is reported`() {
        val code = """
            class Foo(val i: Int?)

            fun test(foo: Foo): Int? {
                val x = foo.i!!
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion inside a binary expression is reported`() {
        val code = """
            fun test(i: Int?) {
                val x = i!! + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion in an argument of a returned call is reported`() {
        val code = """
            fun test(a: Int?): Int? {
                return check(a!!)
            }

            fun check(i: Int): Int = i
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion in a Unit lambda argument is reported`() {
        val code = """
            fun test(list: List<String>, number: Int?) {
                list.forEach {
                    number!!
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion in a lambda with a backticked label is reported`() {
        val code = """
            fun test(list: List<String>, number: Int?) {
                list.forEach `foo bar`@{
                    number!!
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion in an anonymous function is reported`() {
        val code = """
            fun test(): Any {
                return (fun(a: Any?) {
                    a!!
                })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a not-null assertion in a lambda with a non-Unit return type passes`() {
        val code = """
            fun test(list: List<String>, number: Int?) {
                val x: List<Int> = list.map {
                    number!!
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a not-null assertion in a lambda without a label passes`() {
        val code = """
            fun test(number: Int?) {
                val action = {
                    number!!
                    Unit
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a not-null assertion in a function with a non-null return type passes`() {
        val code = """
            fun test(i: Int?): Int {
                val x = i!!
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned not-null assertion passes`() {
        val code = """
            fun test(i: Int?): Int? {
                return i!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned parenthesized not-null assertion passes`() {
        val code = """
            fun test(i: Int?): Int? {
                return (i!!)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unnecessary not-null assertion passes`() {
        val code = """
            fun test(i: Int) {
                val x = i!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
