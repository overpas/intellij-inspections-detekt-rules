package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class AddVarianceModifierTest {

    private val environment = createEnvironment()

    private val sut = AddVarianceModifier(Config.empty)

    @Test
    fun `a type parameter used only in return types is reported`() {
        val code = """
            interface Producer<T> {
                fun produce(): T
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type parameter used only in parameter types is reported`() {
        val code = """
            interface Consumer<T> {
                fun consume(item: T)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type parameter used only in a read-only property is reported`() {
        val code = """
            abstract class Source<T> {
                abstract val item: T
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type parameter used only in a primary constructor parameter is reported`() {
        val code = """
            class Sink<T>(private val handler: (T) -> Unit)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type parameter used as a type argument of a return type is reported`() {
        val code = """
            interface Repository<T> {
                fun all(): List<T>
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type parameter used in both input and output positions passes`() {
        val code = """
            interface Box<T> {
                fun get(): T
                fun put(item: T)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter used in a mutable property passes`() {
        val code = """
            interface Cell<T> {
                var value: T
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter with a declared variance passes`() {
        val code = """
            interface Producer<out T> {
                fun produce(): T
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter that appears in its own bound passes`() {
        val code = """
            interface Node<T : Comparable<T>> {
                fun value(): T
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter used only in private members passes`() {
        val code = """
            class Cache<T> {
                private fun read(): T? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter of a function passes`() {
        val code = """
            fun <T> produce(item: T): List<T> = listOf(item)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unused type parameter passes`() {
        val code = """
            class Marker<T>
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
