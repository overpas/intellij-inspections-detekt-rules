package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterIsInstanceCallWithClassLiteralArgumentTest {

    private val environment = createEnvironment()

    private val sut = FilterIsInstanceCallWithClassLiteralArgument(Config.empty)

    @Test
    fun `a class literal argument on a list is reported`() {
        val code = """
            fun foo(list: List<*>) {
                list.filterIsInstance(Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class literal argument on an array is reported`() {
        val code = """
            fun foo(array: Array<*>) {
                array.filterIsInstance(Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified class literal argument is reported`() {
        val code = """
            fun foo(list: List<*>) {
                list.filterIsInstance(kotlin.Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class literal argument with an explicit type argument is reported`() {
        val code = """
            fun foo(list: List<*>) {
                list.filterIsInstance<Int>(Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class literal argument of a class with a companion object is reported`() {
        val code = """
            class A {
                companion object
            }

            fun foo(list: List<Any>) {
                list.filterIsInstance(A::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a class literal argument of a generic class passes`() {
        val code = """
            class A<T, U>

            fun foo(list: List<Any>) {
                list.filterIsInstance(A::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified class literal argument of a generic class passes`() {
        val code = """
            package pack

            class A<T, U>

            fun foo(list: List<Any>) {
                list.filterIsInstance(pack.A::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class variable argument passes`() {
        val code = """
            fun foo(list: List<*>, klass: Class<Int>) {
                list.filterIsInstance(klass)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reified type argument passes`() {
        val code = """
            fun foo(list: List<*>) {
                list.filterIsInstance<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom filterIsInstance function passes`() {
        val code = """
            fun <T> List<*>.filterIsInstance(klass: Class<T>): List<T> = emptyList()

            fun foo(list: List<*>) {
                list.filterIsInstance(Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
