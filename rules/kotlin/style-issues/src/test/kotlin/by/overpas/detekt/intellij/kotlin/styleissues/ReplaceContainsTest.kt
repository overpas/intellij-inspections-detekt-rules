package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceContainsTest {

    private val environment = createEnvironment()

    private val sut = ReplaceContains(Config.empty)

    @Test
    fun `a contains operator call is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun contains(a: Int): Boolean = true
                }
                val test = Test()
                test.contains(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated contains operator call is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun contains(a: Int): Boolean = true
                }
                val test = Test()
                if (!test.contains(1)) return
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains operator extension call is reported`() {
        val code = """
            object A

            operator fun A.contains(x: Int): Boolean = x > 0

            fun test() {
                A.contains(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains operator call with a lambda argument is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun contains(fn: () -> Boolean): Boolean = fn()
                }
                val test = Test()
                test.contains {
                    true
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains operator call on a companion object is reported`() {
        val code = """
            class C {
                companion object {
                    operator fun contains(s: String) = s.isEmpty()
                }
            }

            fun foo() {
                C.contains("x")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains call on a string is reported`() {
        val code = """
            fun test() {
                val foo = "foo"
                foo.contains("bar")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains call on a list is reported`() {
        val code = """
            fun test(list: List<Int>) = list.contains(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains call of a Java method is reported`() {
        val code = """
            fun test(list: java.util.ArrayList<Int>) = list.contains(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains call with explicit type arguments is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun <T> contains(a: T): Boolean = a == null
                }
                val test = Test()
                test.contains<Int>(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a contains call without the operator modifier passes`() {
        val code = """
            fun test() {
                class Test {
                    fun contains(a: Int): Boolean = a > 0
                }
                val test = Test()
                test.contains(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a contains call on super passes`() {
        val code = """
            open class Base {
                open operator fun contains(s: String) = true
            }

            class C : Base() {
                override fun contains(s: String): Boolean {
                    return super.contains(s)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a contains call with two arguments passes`() {
        val code = """
            fun test() {
                val foo = "foo"
                foo.contains("bar", true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
