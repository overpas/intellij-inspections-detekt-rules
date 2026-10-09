package by.overpas.detekt.intellij.kotlin.javainterop

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaCollectionsStaticMethodOnImmutableListTest {

    private val environment = createEnvironment()

    private val sut = JavaCollectionsStaticMethodOnImmutableList(Config.empty)

    @Test
    fun `a reverse call on an immutable list is reported`() {
        val code = """
            import java.util.Collections

            fun test() {
                val immutableList = listOf(1, 2)
                Collections.reverse(immutableList)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sort call on an immutable list is reported`() {
        val code = """
            import java.util.Collections

            fun test(list: List<String>) {
                Collections.sort(list)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a sort call with a lambda comparator on an immutable list is reported`() {
        val code = """
            import java.util.Collections

            fun test(list: List<String>) {
                Collections.sort(list) { a, b -> a.length - b.length }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a shuffle call on an immutable list is reported`() {
        val code = """
            import java.util.Collections
            import java.util.Random

            fun test(list: List<String>) {
                Collections.shuffle(list)
                Collections.shuffle(list, Random(1))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a reverse call on a mutable list passes`() {
        val code = """
            import java.util.Collections

            fun test() {
                val mutableList = mutableListOf(1, 2)
                Collections.reverse(mutableList)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reverse call on a Java array list passes`() {
        val code = """
            import java.util.Collections

            fun test(list: java.util.ArrayList<Int>) {
                Collections.reverse(list)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a sort call with a comparator object passes`() {
        val code = """
            import java.util.Collections

            fun test(list: List<String>) {
                Collections.sort(list, Comparator.naturalOrder())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-mutating Collections call on an immutable list passes`() {
        val code = """
            import java.util.Collections

            fun test(list: List<String>) = Collections.unmodifiableList(list)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call of a same-named function of another class passes`() {
        val code = """
            object Collections {
                fun reverse(list: List<Int>) = list.reversed()
            }

            fun test() = Collections.reverse(listOf(1, 2))
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
