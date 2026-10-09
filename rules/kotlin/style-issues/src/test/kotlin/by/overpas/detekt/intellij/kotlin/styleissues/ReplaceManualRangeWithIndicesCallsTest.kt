package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceManualRangeWithIndicesCallsTest {

    private val environment = createEnvironment()

    private val sut = ReplaceManualRangeWithIndicesCalls(Config.empty)

    @Test
    fun `ranges outside of a loop are reported`() {
        val code = """
            fun test(args: Array<String>, list: List<String>) {
                val a = 0..args.size - 1
                val b = 0 until args.size
                val c = 0..<args.size
                val d = 0.rangeTo(list.size - 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `a range used as a receiver is reported`() {
        val code = """
            fun foo(list: List<String>) {
                (0 until list.size).forEach {
                    println(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `ranges over an implicit and an explicit this receiver are reported`() {
        val code = """
            fun Array<String>.test() {
                val a = 0 until size
                val b = 0..this.size - 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a for loop that reads only elements is reported`() {
        val code = """
            fun test(args: Array<String>) {
                for (index in 0..args.size - 1) {
                    val out = args[index]
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop that reads elements with get is reported`() {
        val code = """
            fun test(args: Array<String>) {
                for (index in 0..args.size - 1) {
                    val out = args.get(index)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop that uses the index and the element is reported`() {
        val code = $$"""
            fun test() {
                val list = listOf("a", "b", "c")
                for (i in 0 until list.size) {
                    println("Index $i: ${list[i]}")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop up to the last index is reported`() {
        val code = """
            fun foo() {
                val intArray = intArrayOf(1, 2, 3, 4, 5)
                for (i in 0..intArray.lastIndex) {
                    println(intArray[i])
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop over a string length is reported`() {
        val code = """
            class Person(val name: String)

            fun getName(person: Person) {
                for (i in 0 until person.name.length) {
                    println(person.name[i])
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop with a labeled this receiver is reported`() {
        val code = """
            fun IntArray.arrayToString(): String = buildString {
                for (i in 0 until size) {
                    append(this@arrayToString[i])
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a for loop that writes elements is reported`() {
        val code = """
            fun test(args: Array<String>) {
                for (index in 0..args.size - 1) {
                    args[index] = "Hello"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a custom iterable with a size and an element access is reported`() {
        val code = """
            class CustomIterable<T>(private val elements: List<T>) : Iterable<T> {
                val size: Int get() = elements.size
                operator fun get(index: Int): T = elements[index]
                override fun iterator(): Iterator<T> = elements.iterator()
            }

            fun test(iterable: CustomIterable<String>) {
                for (i in 0 until iterable.size) {
                    println(iterable[i])
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a custom iterable without indices and without an element access passes`() {
        val code = """
            class CustomIterable<T>(private val elements: List<T>) : Iterable<T> {
                val size: Int get() = elements.size
                val indices: String = "wrong type"
                override fun iterator(): Iterator<T> = elements.iterator()
            }

            fun test(iterable: CustomIterable<String>) {
                for (i in 0 until iterable.size) {
                    println(i)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map size passes`() {
        val code = """
            fun test(map: Map<*, *>) {
                for (i in 0 until map.size) {
                    println(i)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local size variable passes`() {
        val code = """
            fun main() {
                val size = 2
                for (i in 0 until size) {
                    println("I repeat everything twice")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a size member of an implicit receiver without indices passes`() {
        val code = """
            class CustomObject {
                val size: Int = 5
                val length: Int = 5
            }

            fun CustomObject.processItems(): List<Int> = (0 until size).map { it } + (0 until length).map { it }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open range up to the last index passes`() {
        val code = """
            fun test(list: List<String>) {
                val x = 42 in 0..<list.lastIndex
                val y = 42 in 0 until list.lastIndex
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a range that does not start at zero passes`() {
        val code = """
            fun test(list: List<String>) {
                val x = 1 until list.size
                val y = list.size downTo 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a loop over an array passes`() {
        val code = """
            fun test(args: Array<Int>) {
                val x = arrayOf<String>()
                for (index in args) {
                    val out = x[index]
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
