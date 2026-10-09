package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceAddAllWithMapToTest {

    private val environment = createEnvironment()

    private val sut = ReplaceAddAllWithMapTo(Config.empty)

    @Test
    fun `addAll of a filter result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1.addAll(coll2.filter { true })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `addAll of a map result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1.addAll(coll2.map { it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `addAll on an implicit receiver is reported`() {
        val code = """
            fun MutableCollection<String>.test(coll2: List<String>) {
                addAll(coll2.map { it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `addAll of a map call on an implicit receiver is reported`() {
        val code = """
            fun List<String>.test(coll1: MutableCollection<String>) {
                coll1.addAll(map { it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `addAll on an outer implicit receiver is reported`() {
        val code = """
            fun MutableList<String>.foo() {
                "hello".run {
                    addAll(listOf("").map { it })
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plusAssign call with a filter result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1.plusAssign(coll2.filter { true })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plusAssign call with a map result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1.plusAssign(coll2.map { it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus assignment of a filter result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1 += coll2.filter { true }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a plus assignment of a map result is reported`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1 += coll2.map { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe addAll call passes`() {
        val code = """
            fun test(coll1: MutableCollection<String>?, coll2: List<String>) {
                coll1?.addAll(coll2.map { it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `addAll of a plain list passes`() {
        val code = """
            fun test(coll1: MutableCollection<String>, coll2: List<String>) {
                coll1.addAll(coll2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plus assignment that adds a single list element passes`() {
        val code = """
            fun test() {
                val result = mutableListOf<List<String>>()
                result += listOf(1, 2, 3).map { it.toString() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plusAssign call that adds a single list element passes`() {
        val code = """
            fun test() {
                val result = mutableListOf<List<String>>()
                result.plusAssign(listOf(1, 2, 3).map { it.toString() })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
