package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousEqualsCombinationTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousEqualsCombination(Config.empty)

    @Test
    fun `a variable compared with both kinds of equality is reported`() {
        val code = """
            object Main {
                val CONST1 = Main
                val CONST2 = Main
                val CONST3 = Main

                fun test(type: Main): Boolean = type === CONST1 || type == CONST2 && type === CONST3
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comparison in parentheses is reported`() {
        val code = """
            object Main {
                val CONST1 = Main
                val CONST2 = Main
                val CONST3 = Main

                fun test(type: Main): Boolean = type === CONST1 || type == CONST2 && (type === CONST3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated comparison is reported`() {
        val code = """
            object Main {
                val CONST1 = Main
                val CONST2 = Main
                val CONST3 = Main

                fun test(type: Main): Boolean = type === CONST1 || type == CONST2 && !(type === CONST3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a constant compared with both kinds of equality is reported`() {
        val code = """
            object Main {
                val CONST1 = Main
                val CONST3 = Main

                fun test(type: Main, type1: Main): Boolean = type === CONST1 || type1 == CONST1 && type === CONST3
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a combination with a null check and a second variable is reported`() {
        val code = """
            object Main {
                fun test(type: Main, type1: Main, type2: Main?, type4: Main?): Boolean =
                    type4 == null || type === type2 || type1 == type
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `different variables compared with different kinds of equality pass`() {
        val code = """
            object Main {
                val CONST1 = Main
                val CONST2 = Main
                val CONST3 = Main

                fun test(type: Main, type1: Main): Boolean = type === CONST1 || type1 == CONST2 && type === CONST3
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check against null passes`() {
        val code = """
            object Main {
                fun test(type: Main, type2: Main?): Boolean = type2 == null || type === type2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an identity check against null passes`() {
        val code = """
            object Main {
                fun test(type: Main, type3: Main?): Boolean = type3 === null || type == type3
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single kind of equality passes`() {
        val code = """
            fun test(a: Any, b: Any, c: Any): Boolean = a == b || a == c
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
