package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyNestedEachInScopeFunctionTest {

    private val environment = createEnvironment()

    private val sut = SimplifyNestedEachInScopeFunction(Config.empty)

    @Test
    fun `a forEach on the implicit parameter of also is reported`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).also { it.forEach { println(it) } }.forEach { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a forEach on the named parameter of also is reported`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).also { s -> s.forEach { println(it) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `nested calls with function references are reported`() {
        val code = """
            fun foo(num: Int) = println(num + 1)

            fun test(): List<Int> = listOf(1, 2, 3).also { it.forEach(::foo) }.apply { forEach(::foo) }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a forEach with a return to its own label is reported`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).also { it.forEach { i ->
                    if (i % 2 == 0) return@forEach
                } }
                listOf(1, 2, 3).also myLabel@{ it.forEach { item ->
                    if (item == 2) return@forEach
                    println(item)
                } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an onEach on a string in also is reported`() {
        val code = """
            fun test(): String = "abc".also { it.onEach { c -> println(c) } }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a forEach on the receiver of apply is reported`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).apply { forEach { println(it) } }
                listOf(1, 2, 3).apply { this@apply.forEach { println(it) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a forEach on another receiver in also passes`() {
        val code = """
            fun test() {
                val a = listOf(1, 2, 3)
                "".also { a.forEach { println(it) } }
                listOf(1, 2, 3).also { listOf(1, 2, 3).forEach { } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach that uses the parameter of also passes`() {
        val code = """
            fun test() {
                mutableListOf(1, 2, 3).also { list -> list.forEach { list.add(it) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach that returns to the scope function label passes`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).also a@{ it.forEach { i ->
                    if (i % 2 == 0) return@a
                } }
                listOf(1, 2, 3).also { it.forEach { i ->
                    if (i % 2 == 0) return@also
                } }
                listOf(1, 2, 3).apply { forEach { this@apply.contains(it) } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach on the receiver of an outer apply passes`() {
        val code = """
            fun test() {
                listOf(1, 2, 3).apply { 1.apply { forEach { println(it + 1) } } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a forEach that uses the receiver of apply passes`() {
        val code = """
            class Test<T>(list: List<T>) : List<T> by list {
                fun print(item: T) {
                    println(item)
                }
            }

            val <T> List<T>.bar
                get() = 1

            fun consumeList(list: List<Int>) = println(list)

            fun test(list: List<Int>) {
                Test(listOf(1, 2, 3)).apply { forEach { print(it) } }
                Test(listOf(1, 2, 3)).apply { this.forEach { this.print(it) } }
                list.apply { this.forEach { println(bar) } }
                list.apply { forEach { _ -> consumeList(this) } }
                1.apply { listOf(1, 2, 3).apply { forEach { this.plus(it) } } }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
