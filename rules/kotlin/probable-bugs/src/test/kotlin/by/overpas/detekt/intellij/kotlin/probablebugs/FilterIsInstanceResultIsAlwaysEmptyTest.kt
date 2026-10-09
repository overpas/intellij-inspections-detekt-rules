package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterIsInstanceResultIsAlwaysEmptyTest {

    private val environment = createEnvironment()

    private val sut = FilterIsInstanceResultIsAlwaysEmpty(Config.empty)

    @Test
    fun `a filterIsInstance call on a list with an unrelated final target is reported`() {
        val code = """
            class A

            fun foo() {
                val filtered = listOf(A()).filterIsInstance<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call with a class argument is reported`() {
        val code = """
            class A

            fun foo() {
                val filtered = listOf(A()).filterIsInstance(Int::class.java)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstanceTo call on an array is reported`() {
        val code = """
            class A

            fun foo() {
                val filtered = arrayOf(A()).filterIsInstanceTo<Int, MutableList<Int>>(mutableListOf())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call on a sequence is reported`() {
        val code = """
            class A

            fun foo() {
                val filtered = sequenceOf(A()).filterIsInstance<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `chained filterIsInstance calls with unrelated targets are reported`() {
        val code = """
            class A

            fun foo() {
                val filtered = listOf(A()).filterIsInstance<Int>().filterIsInstance<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a filterIsInstance call between two unrelated open classes is reported`() {
        val code = """
            open class A
            open class B

            inline fun <reified T : B, R : A> foo(list: List<R>) {
                val filtered = list.filterIsInstance<T>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call on elements bounded by an interface with a final target is reported`() {
        val code = """
            interface A

            fun <T : A> foo(list: List<T>) {
                val filtered = list.filterIsInstance<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a filterIsInstance call with a subtype target passes`() {
        val code = """
            open class A
            class B : A()

            fun foo() {
                val filtered = listOf(A()).filterIsInstance<B>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call with a supertype target passes`() {
        val code = """
            open class A
            class B : A()

            fun foo() {
                val filtered = sequenceOf(B()).filterIsInstance<A>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call with an interface target on an open class passes`() {
        val code = """
            interface I
            open class Base
            class Impl : Base(), I

            fun foo() {
                val list: List<Base> = listOf(Impl())
                val filtered = list.filterIsInstance<I>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call on elements with a class and an interface bound passes`() {
        val code = """
            interface A
            open class B
            class C : A, B()

            fun <T> foo(list: List<T>) where T : A, T : B {
                val filtered = list.filterIsInstance<C>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call with a target bounded by the element type passes`() {
        val code = """
            open class A

            inline fun <reified R : T, T : A> foo(list: List<T>) {
                val filtered = list.filterIsInstance<R>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a filterIsInstance call with unbounded type parameters passes`() {
        val code = """
            inline fun <T, reified R> foo(list: List<T>) {
                val filtered = list.filterIsInstance<R>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
