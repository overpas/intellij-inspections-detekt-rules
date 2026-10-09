package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionConcatenationToBuildCollectionTest {

    private val environment = createEnvironment()

    private val sut = CollectionConcatenationToBuildCollection(Config.empty)

    @Test
    fun `a concatenation of three sets is reported`() {
        val code = """
            fun test(set1: Set<String>, set2: Set<String>) {
                val result = set1 + set2 + set2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a concatenation of three lists is reported`() {
        val code = """
            fun test() {
                val list1 = listOf("foo", "bar")
                val list2 = listOf("q", "b")
                val result = list1 + list2 + listOf("a", "", "ccc")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a long concatenation is reported once`() {
        val code = """
            fun test() {
                val result = listOf(1, 2) + listOf(3, 4) + 5 + listOf(6, 7) + 8
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a concatenation of elements, sequences and sets onto a list is reported`() {
        val code = """
            fun test() {
                val result = listOf(1, 2) +
                    listOf(3, 4) +
                    5 +
                    sequenceOf(6, 7) +
                    setOf(1, 2) +
                    hashSetOf(1) +
                    mutableListOf(1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a concatenation with removals is reported`() {
        val code = """
            fun test(list: List<Int>) {
                val result = setOf(1, 2) - list + 3 - 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a concatenation of transformed lists is reported`() {
        val code = """
            fun test(list: List<Int?>) {
                val result = listOf(1, 2) +
                    list.filterNotNull().map { it + 1 } +
                    list.mapNotNull { it } +
                    list.filterIsInstance<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized concatenation with a qualified call is reported`() {
        val code = """
            fun test(list: List<Int>): Int =
                (list + list.size + setOf(list.indexOf(1))).count()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a concatenation inside a builder is reported`() {
        val code = """
            val x = buildSet<Boolean> {
                add(true)
                addAll(
                    listOf(this).flatten() + this.map { !it } + this.mapTo(this) { !it },
                )
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single concatenation passes`() {
        val code = """
            fun test(set1: Set<String>, set2: Set<String>) {
                val result = set1 + set2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation with a parenthesized operand passes`() {
        val code = """
            fun test() {
                val result = listOf(1, 2) + (listOf(1) - 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation with a custom member operator passes`() {
        val code = """
            class MyList(element: String) : List<String> by listOf(element) {
                operator fun plus(other: MyList): MyList = MyList(this.toString() + other.toString())
            }

            fun test() {
                val result = MyList("a") + MyList("b") + MyList("c")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation with a custom extension operator passes`() {
        val code = """
            operator fun List<Int>.plus(other: List<Int>): List<Int> = emptyList()

            fun test() {
                val result = listOf(1) + listOf(2) + listOf(3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation of maps passes`() {
        val code = """
            fun test() {
                val result = mapOf(1 to 2) + mapOf(2 to 3) + mapOf(3 to 4)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a concatenation of sequences passes`() {
        val code = """
            fun test() {
                val result = sequenceOf(1) + 2 + sequenceOf(3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a set builder passes`() {
        val code = """
            fun test(set1: Set<String>, set2: Set<String>, set3: Set<String>) {
                val set = buildSet {
                    addAll(set1)
                    addAll(set2)
                    addAll(set3)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
