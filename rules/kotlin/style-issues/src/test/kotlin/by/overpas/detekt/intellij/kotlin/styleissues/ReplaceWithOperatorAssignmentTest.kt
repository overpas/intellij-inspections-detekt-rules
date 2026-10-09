package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithOperatorAssignmentTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithOperatorAssignment(Config.empty)

    @Test
    fun `an addition to the assigned variable is reported`() {
        val code = """
            fun foo() {
                var x = 0
                x = x + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subtraction from the assigned variable is reported`() {
        val code = """
            fun foo() {
                var x = 0
                x = x - 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an addition with the variable on the right side is reported`() {
        val code = """
            fun foo() {
                var x = 0
                x = 1 + x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an addition of another variable is reported`() {
        val code = """
            fun foo() {
                var y = 0
                val x = 0
                y = y + x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a chain of additions starting with the variable is reported`() {
        val code = """
            fun foo() {
                var x = 0
                val y = 0
                x = x + y + 5
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a chain of additions with the variable in the middle is reported`() {
        val code = """
            fun foo() {
                var x = 0
                val y = 0
                x = y + x + 5
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an addition to a property of a parameter is reported`() {
        val code = """
            class Data(var quantity: Int)

            fun increment(item: Data) {
                item.quantity = item.quantity + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subtraction of the variable from a value passes`() {
        val code = """
            fun foo() {
                var x = 0
                x = 1 - x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assignment that does not repeat the variable passes`() {
        val code = """
            fun foo() {
                var x = 0
                val y = 0
                val z = 0
                x = y + z
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a chain of mixed operators passes`() {
        val code = """
            fun foo() {
                var x = 0
                x = x / 1 + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a chain with the variable in a division on the left passes`() {
        val code = """
            fun foo() {
                var x = 0
                val y = 0
                x = y / x + 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a chain of subtractions passes`() {
        val code = """
            fun foo() {
                var x = 0
                x = x - 1 - 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an addition with both plus and plusAssign operators passes`() {
        val code = """
            class A

            operator fun A.plus(a: A): A = A()

            operator fun A.plusAssign(a: A) {}

            fun foo() {
                var a1 = A()
                val a2 = A()
                a1 = a1 + a2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an addition to a read-only list passes`() {
        val code = """
            fun foo() {
                var list = listOf(1, 2, 3)
                list = list + 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an addition to a platform list passes`() {
        val code = """
            fun foo() {
                var list1 = java.util.Collections.emptyList<String>()
                val list2 = listOf("b")
                list1 = list1 + list2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
