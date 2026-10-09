package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ForEachParameterNotUsedTest {

    private val environment = createEnvironment()

    private val sut = ForEachParameterNotUsed(Config.empty)

    @Test
    fun `a forEach call with an empty lambda is reported`() {
        val code = """
            fun test(list: List<String>) {
                list.forEach {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe forEach call with an unused parameter is reported`() {
        val code = """
            fun test(list: List<String>?) {
                list?.forEach {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a forEach call on a sequence with an unused parameter is reported`() {
        val code = """
            fun test(sequence: Sequence<String>) {
                sequence.forEach { println() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a forEach call on a string with an unused parameter is reported`() {
        val code = """
            fun test(s: String) {
                s.forEach {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an outer forEach call whose parameter is shadowed by an inner lambda is reported`() {
        val code = """
            fun test(items: List<Any>) {
                items.forEach { items.forEach { println(it) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a forEach call that uses its implicit parameter passes`() {
        val code = """
            fun test(items: List<Any>) {
                items.forEach { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach call with an explicit parameter passes`() {
        val code = """
            fun test(items: List<Any>) {
                items.forEach { item -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an outer forEach call whose parameter is used in an inner lambda passes`() {
        val code = """
            fun test(items: List<Any>) {
                items.forEach { items.forEach { thing -> println(it); println(thing) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach call that invokes its implicit parameter passes`() {
        val code = """
            class My {
                operator fun invoke() {}
            }

            fun bar(my: List<My>) {
                my.forEach { it() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach call with a destructured parameter passes`() {
        val code = $$"""
            fun foo(map: Map<String, String>) {
                map.forEach { (t, u) -> println("$t: $u") }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member forEach call passes`() {
        val code = """
            class ForEachable {
                fun forEach(action: (ForEachable) -> Unit) {}
            }

            fun test() {
                ForEachable().forEach {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
