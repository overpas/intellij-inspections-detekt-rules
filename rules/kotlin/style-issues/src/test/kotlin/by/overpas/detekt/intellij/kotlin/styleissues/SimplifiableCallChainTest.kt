package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifiableCallChainTest {

    private val environment = createEnvironment()

    private val sut = SimplifiableCallChain(Config.empty)

    @Test
    fun `a filter followed by first is reported`() {
        val code = """
            val x = listOf(1, 2, 3).filter { it > 1 }.first()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter followed by isEmpty is reported`() {
        val code = """
            val x = listOf(1, 2, 3).filter { it % 2 != 0 }.isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter on a sequence followed by count is reported`() {
        val code = """
            val x = sequenceOf(1, 2, 3).filter { it > 1 }.count()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter on a string followed by singleOrNull is reported`() {
        val code = """
            val x = "5abc".filter { it.isDigit() }.singleOrNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter on a primitive array followed by last is reported`() {
        val code = """
            val x = intArrayOf(0, 1, 2, 3).filter { it > 0 }.last()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sortedBy with a non-null selector followed by first is reported`() {
        val code = """
            val x = listOf("a" to 1, "c" to 3).sortedBy { it.second }.first()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map followed by joinToString is reported`() {
        val code = """
            val x = listOf(1, 2, 3).map(Int::toString).joinToString(prefix = "= ", separator = " + ")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map followed by sum of a variable is reported`() {
        val code = """
            fun test(list: List<Int>, i: Int) {
                list.map { if (i == 1) i else 3 }.sum()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a map followed by toMap is reported`() {
        val code = """
            fun test(list: List<Int>) {
                val map: Map<Int, String> = list.map { it to it.toString() }.toMap()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a listOf followed by filterNotNull is reported`() {
        val code = """
            val s: String? = null
            val x = listOf(s).filterNotNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapIndexed followed by flatten is reported`() {
        val code = """
            fun test() {
                sequenceOf(1, 2, 3).mapIndexed { i, x -> sequenceOf(i, x) }.flatten()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter followed by first with a predicate passes`() {
        val code = """
            val x = listOf("1", "").filter { it.isNotEmpty() }.first { it[0] == 'A' }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map on a map followed by joinToString passes`() {
        val code = """
            fun test(data: Map<String, String>) {
                val result = data.map { it.key + it.value }.joinToString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map to a non-string followed by joinToString passes`() {
        val code = """
            val x = listOf(1, 2, 3).map(Int::toDouble).joinToString(prefix = "= ", separator = " + ")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map with a suspend call followed by joinToString passes`() {
        val code = """
            suspend fun mapString(input: String): String = input

            suspend fun test() {
                val x = listOf("1", "2", "3").map { mapString(it) }.joinToString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map to floats followed by sum passes`() {
        val code = """
            fun test() {
                listOf(0.1f, 0.2f).map { it * it }.sum()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map to an int literal followed by sum passes`() {
        val code = """
            fun test(list: List<Int>, i: Int) {
                list.map {
                    when (i) {
                        1 -> 1
                        else -> 3
                    }
                }.sum()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sortedBy with a nullable selector followed by first passes`() {
        val code = """
            fun test() {
                listOf(1, null, 2).sortedBy { it }.first()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map on a primitive array followed by filterNotNull passes`() {
        val code = """
            fun test() {
                intArrayOf(0, 1, 2, 3).map { if (it > 0) it else null }.filterNotNull()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map with a return followed by joinToString passes`() {
        val code = """
            fun test(args: List<Int>): String {
                return args.map {
                    if (it == 0) return ""
                    it.toString()
                }.joinToString(separator = " + ")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
