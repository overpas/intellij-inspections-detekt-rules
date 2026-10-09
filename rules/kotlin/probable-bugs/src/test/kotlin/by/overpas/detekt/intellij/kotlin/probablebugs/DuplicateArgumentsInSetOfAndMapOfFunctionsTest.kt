package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class DuplicateArgumentsInSetOfAndMapOfFunctionsTest {

    private val environment = createEnvironment()

    private val sut = DuplicateArgumentsInSetOfAndMapOfFunctions(Config.empty)

    @Test
    fun `duplicate keys in mapOf are reported`() {
        val code = """
            fun a() {
                mapOf(1 to 1, 1 to 2, 3 to 3)
                setOf(1, "1", 3, 4, 5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a duplicate constant in setOf is reported`() {
        val code = """
            const val b = 1

            fun a() {
                setOf(1, b, 3, 4, 5, 6, 7, 8, 9, 10)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `duplicate nulls in setOf are reported`() {
        val code = """
            fun a() {
                setOf(null, null, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `duplicate strings in mutableSetOf are reported`() {
        val code = """
            fun a() {
                mutableSetOf("a", "b", "a", "a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `duplicate keys in hashMapOf are reported`() {
        val code = """
            fun a() {
                hashMapOf("a" to 1, "a" to 2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `distinct elements in setOf pass`() {
        val code = """
            fun a() {
                setOf(1, "1", 3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `distinct keys in mapOf pass`() {
        val code = """
            fun a() {
                mapOf(1 to 1, "1" to 2, 3 to 3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `duplicate values in mapOf pass`() {
        val code = """
            fun a() {
                mapOf(1 to "a", 2 to "a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `duplicate non-constant elements in setOf pass`() {
        val code = """
            fun a(x: Int) {
                setOf(x, x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `duplicate elements in listOf pass`() {
        val code = """
            fun a() {
                listOf(1, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `duplicate elements in a user function named setOf pass`() {
        val code = """
            fun <T> setOf(vararg elements: T): List<T> = elements.toList()

            fun a() {
                setOf(1, 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
