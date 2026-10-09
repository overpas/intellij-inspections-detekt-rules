package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousCollectionReassignmentTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousCollectionReassignment(Config.empty)

    @Test
    fun `a plus assignment on a local read-only list is reported`() {
        val code = """
            fun test() {
                var list = listOf(1)
                list += 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus assignment on a local read-only set is reported`() {
        val code = """
            fun test() {
                var set = setOf(1)
                set -= 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus assignment on a local read-only map is reported`() {
        val code = """
            fun test() {
                var map = mapOf(1 to 2)
                map += 3 to 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a minus assignment of a list on a read-only list property is reported`() {
        val code = """
            class Test {
                var list = listOf(1)

                fun test() {
                    list -= listOf(1)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus assignment on a read-only list property passes`() {
        val code = """
            var list = listOf(1)

            fun test() {
                list += 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a minus assignment of a list on a read-only map property passes`() {
        val code = """
            var map = mapOf(1 to 2)

            fun test() {
                map -= listOf(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus assignment on an Int passes`() {
        val code = """
            fun test() {
                var i = 1
                i += 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `plain plus and minus on a read-only list pass`() {
        val code = """
            fun test() {
                var list = listOf(1)
                list + 2
                list - 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `plus assignments on mutable collections pass`() {
        val code = """
            fun test() {
                var list = mutableListOf(1)
                list += 2
                var set = mutableSetOf(1)
                set += 2
                var map = mutableMapOf(1 to 2)
                map += 3 to 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus assignment on a val passes`() {
        val code = """
            fun test() {
                val list = mutableListOf(1)
                list += 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus assignment on a local read-only list without an initializer passes`() {
        val code = """
            fun test(flag: Boolean) {
                var list: List<Int>
                list = if (flag) listOf(1) else emptyList()
                list += 2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
