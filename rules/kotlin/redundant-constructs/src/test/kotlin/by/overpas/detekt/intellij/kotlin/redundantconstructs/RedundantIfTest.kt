package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantIfTest {

    private val environment = createEnvironment()

    private val sut = RedundantIf(Config.empty)

    @Test
    fun `an if that returns true or false is reported`() {
        val code = """
            fun foo(value: Int): Boolean {
                if (value % 2 == 0) {
                    return true
                } else {
                    return false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if with a boolean expression branch is reported`() {
        val code = """
            fun baz(value: Int): Boolean {
                if (value % 2 == 0) return value > 10 else return false
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if expression with is and comparison operands is reported`() {
        val code = """
            data class Box(val value: Int, val name: String)

            fun withIs(value: Any?, box: Box): Boolean {
                return if (value is String) true else box.name != "fallback"
            }

            fun withIn(value: Int, box: Box): Boolean {
                return if (value in 1..10) box.value == 0 else true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an if without else followed by a return is reported`() {
        val code = """
            fun foo(list: List<String>): Boolean {
                if (list.isEmpty()) return false
                return true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if with labeled returns is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).find {
                    if (it > 0) {
                        return@find true
                    } else {
                        return@find false
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if that assigns true or false to one variable is reported`() {
        val code = """
            fun bar(p: Int) {
                var v = false
                if (p > 0) v = true else v = false
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if with only one commented branch is reported`() {
        val code = """
            fun foo(): Boolean {
                if (someComplexCondition()) return true
                return someOtherCondition() // comment explaining the fallback case
            }

            fun someComplexCondition(): Boolean = true

            fun someOtherCondition(): Boolean = true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if that compares floating point constants for equality is reported`() {
        val code = """
            fun test(): Boolean {
                if (Double.NaN == Double.NaN) return false
                return true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if that assigns to different variables passes`() {
        val code = """
            fun bar(p: Int) {
                var v1 = false
                var v2 = false
                if (p > 0) v2 = true else v1 = false
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a nullable or call branch passes`() {
        val code = """
            fun noChange(value: Int, flag: Boolean?): Boolean? {
                return if (value % 2 == 0) flag else false
            }

            fun noChangeCall(value: Int): Boolean {
                return if (value % 2 == 0) checkValue(value) else false
            }

            fun checkValue(value: Int): Boolean = value > 10
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a non-constant comparison of object fields passes`() {
        val code = """
            class Person(val name: String)

            fun xxx(person1: Person, person2: Person): Boolean {
                if (person1.name != person2.name) return false
                return true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with an operator negation of a non-boolean passes`() {
        val code = """
            operator fun String.not(): Boolean = false

            fun bar(): Boolean {
                if (!"hello") {
                    return false
                } else {
                    return true
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a floating point inequality passes`() {
        val code = """
            fun test(d: Double): Boolean {
                if (42 < d) return false
                return true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with comments in both branches passes`() {
        val code = """
            fun foo(): Boolean {
                if (someComplexCondition()) return false // comment explaining the false case
                return true // comment explaining the true case
            }

            fun someComplexCondition(): Boolean = true
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a chained if passes`() {
        val code = """
            fun b(x: Int): Boolean {
                return if (x > 20) {
                    true
                } else if (x > 0) {
                    true
                } else {
                    false
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if after another if passes`() {
        val code = """
            fun test(a: Boolean, b: Boolean): Boolean {
                if (a) return true
                if (b) return false
                return true
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
