package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceCallWithBinaryOperatorTest {

    private val environment = createEnvironment()

    private val sut = ReplaceCallWithBinaryOperator(Config.empty)

    @Test
    fun `an equals call is reported`() {
        val code = """
            val x = 2.equals(2)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated equals call is reported`() {
        val code = """
            val x = !(2.equals(3))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a compareTo call compared with zero is reported`() {
        val code = """
            val x = 3.compareTo(2) > 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a compareTo call on the right of a comparison with zero is reported`() {
        val code = """
            val x = 0 >= 4.compareTo(5)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus operator call is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun plus(a: Int): Test = Test()
                }
                val test = Test()
                test.plus(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a rangeUntil operator call is reported`() {
        val code = """
            fun test() {
                class Test {
                    operator fun rangeUntil(a: Int): Test = Test()
                }
                val test = Test()
                test.rangeUntil(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus call on doubles is reported`() {
        val code = """
            fun test(a: Double, b: Double) = a.plus(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call on doubles is reported`() {
        val code = """
            fun test(a: Double, b: Double) = a.equals(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call on smart cast doubles is reported`() {
        val code = """
            fun test(a: Any, b: Any) = a is Double && b is Double && a.equals(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus call of a Java method is reported`() {
        val code = """
            fun test(date: java.time.LocalDate) = date.plus(java.time.Period.ofDays(1))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a compareTo call of a Java method is reported`() {
        val code = """
            fun test(a: java.time.Duration, b: java.time.Duration) = a.compareTo(b) < 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call on a platform type is reported`() {
        val code = """
            fun test() = System.getProperty("a").equals(System.getProperty("b"))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between unrelated types passes`() {
        val code = """
            val x = 2.equals("")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call of a smart cast double and an int passes`() {
        val code = """
            fun test(a: Any, b: Any) = a is Double && b is Int && a.equals(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals extension function passes`() {
        val code = """
            class Foo

            fun Foo?.equals(other: Foo?) = other == null

            fun bar(f1: Foo?, f2: Foo?) = f1.equals(f2)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compareTo call compared with zero for equality passes`() {
        val code = """
            val x = 2.compareTo(2) == 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compareTo call with a floating point argument passes`() {
        val code = """
            val value = -0.0f
            val x = 0.compareTo(value) < 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compareTo function without the operator modifier passes`() {
        val code = """
            class Operation {
                fun compareTo(other: Operation) = 0
            }

            fun test(p1: Operation, p2: Operation) = p1.compareTo(p2) < 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus function without the operator modifier passes`() {
        val code = """
            class Operation {
                fun plus(other: Operation) = 0
            }

            fun test(p1: Operation, p2: Operation) = p1.plus(p2)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus call on super passes`() {
        val code = """
            open class Base {
                open operator fun plus(s: String) = ""
            }

            class C : Base() {
                override fun plus(s: String): String {
                    return super.plus(s)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an operator call with explicit type arguments passes`() {
        val code = """
            fun test() {
                class Test {
                    operator fun <T> div(a: Test): T? = a as? T
                }
                val test = Test()
                test.div<Int>(Test())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
