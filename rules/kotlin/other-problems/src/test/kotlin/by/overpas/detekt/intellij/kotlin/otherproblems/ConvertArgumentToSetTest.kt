package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertArgumentToSetTest {

    private val environment = createEnvironment()

    private val sut = ConvertArgumentToSet(Config.empty)

    @Test
    fun `an iterable argument of an infix intersect is reported`() {
        val code = """
            fun <T> f(a: Iterable<T>, b: Iterable<T>) = a intersect b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an iterable argument of an infix subtract is reported`() {
        val code = """
            fun <T> f(a: Iterable<T>, b: Iterable<T>) = a subtract b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an array list operand of minus is reported`() {
        val code = """
            fun <T : CharSequence> foo(a: Iterable<T>): List<T> {
                val b = arrayListOf("a", "b", "c", "e")
                return a - b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mutable list argument of a minus call is reported`() {
        val code = """
            val b = mutableListOf("a", "b", "c", "e")

            fun <T : CharSequence> foo(a: Iterable<T>) = a.minus(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sequence operand of minus assign is reported`() {
        val code = """
            fun foo(a: MutableCollection<Int>, b: Sequence<Int>) {
                a -= b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a list of a shadowed listOf function is reported`() {
        val code = """
            fun <E> listOf(vararg elements: E): List<E> = elements.toList()

            fun <T : CharSequence> foo(a: Iterable<T>): List<T> {
                val b = listOf("a", "b", "c", "e")
                return a - b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a listOf with a non-constant argument is reported`() {
        val code = """
            fun bar(): Int = TODO()

            fun foo(a: Iterable<Int>): List<Int> {
                val b = listOf(1, bar(), 3)
                return a - b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a listOf with more than ten arguments is reported`() {
        val code = """
            fun f(a: Iterable<Int>) = a intersect listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a listOf with ten constant arguments passes`() {
        val code = """
            fun f(a: Iterable<Int>) = a intersect listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a val initialized with constant listOf arrayOf or sequenceOf passes`() {
        val code = """
            fun <T : CharSequence> foo(a: Iterable<T>) {
                val b = listOf("a", "b")
                val c = arrayOf("a", "b")
                val d = kotlin.sequences.sequenceOf("a", "b")
                println(a - b)
                println(a - c)
                println(a - d)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a listOf through an import alias passes`() {
        val code = """
            import kotlin.collections.listOf as someFunction

            fun <T : CharSequence> foo(a: Iterable<T>): List<T> {
                val b = someFunction("a", "b", "c", "e")
                return a - b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a set hidden by an elvis operator passes`() {
        val code = """
            fun foo(a: String, b: String, array: Array<String>?) =
                listOf(a, b) - (((array?.toSet() ?: emptySet()) ?: emptySet()) ?: emptySet())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call of a shadowed intersect function passes`() {
        val code = """
            fun <T> Iterable<T>.intersect(other: Iterable<T>): Set<T> = other.toSet()

            fun foo(a: Iterable<Int>, b: Iterable<Int>) = a.intersect(b)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a set argument passes`() {
        val code = """
            fun foo(a: Iterable<Int>, b: Set<Int>) {
                println(a - b)
                println(a intersect b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
