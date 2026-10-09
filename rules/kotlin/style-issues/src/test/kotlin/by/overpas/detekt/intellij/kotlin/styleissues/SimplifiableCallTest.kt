package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifiableCallTest {

    private val environment = createEnvironment()

    private val sut = SimplifiableCall(Config.empty)

    @Test
    fun `a flatMap with an identity lambda is reported`() {
        val code = """
            fun test() {
                listOf(listOf(1)).flatMap { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flatMap with an explicit identity lambda parameter is reported`() {
        val code = """
            fun test() {
                listOf(listOf(1)).flatMap { i -> i }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flatMap with an identity lambda in parentheses on a set is reported`() {
        val code = """
            fun test() {
                setOf(setOf(1)).flatMap({ it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter of non-null values is reported`() {
        val code = """
            fun test(list: List<String?>) {
                list.filter { it != null }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter of non-null values with a reversed identity check is reported`() {
        val code = """
            fun test(list: List<String?>) {
                list.filter { arg -> null !== arg }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter by type is reported`() {
        val code = """
            fun test(list: List<Any>) {
                list.filter { it is String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filter by type on an implicit receiver is reported`() {
        val code = """
            fun List<Any>.test() {
                filter { it is String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mapNotNull with a safe cast is reported`() {
        val code = """
            fun test(list: List<Any>) {
                list.mapNotNull { value -> value as? String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a flatMap with an identity lambda on an array of lists passes`() {
        val code = """
            fun test() {
                arrayOf(listOf(1), listOf(2)).flatMap { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flatMap with an identity lambda on a list of sequences passes`() {
        val code = """
            fun test() {
                listOf(sequenceOf(1, 2), sequenceOf(3, 4)).flatMap { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a flatMap with a non-identity lambda passes`() {
        val code = """
            fun test() {
                listOf(listOf(1)).flatMap { it + 1 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter by an unrelated type passes`() {
        val code = """
            interface Foo
            interface Bar

            fun test(x: List<Foo>): List<Foo> = x.filter { it is Bar }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter by a negated type check passes`() {
        val code = """
            fun test(list: List<Any>) {
                list.filter { it !is String }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter of map entries passes`() {
        val code = """
            fun test(map: Map<String?, String?>) {
                map.filter { it != null }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filter by a boolean value passes`() {
        val code = """
            fun test(list: List<Boolean>) {
                list.filter { it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mapNotNull with a safe cast of a property passes`() {
        val code = """
            interface X

            class A(val l: Any)

            fun test() {
                listOf<A>().mapNotNull { it.l as? X }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
