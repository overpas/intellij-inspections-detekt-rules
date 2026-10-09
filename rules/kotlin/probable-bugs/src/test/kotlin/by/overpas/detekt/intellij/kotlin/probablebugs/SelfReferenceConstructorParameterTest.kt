package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SelfReferenceConstructorParameterTest {

    private val environment = createEnvironment()

    private val sut = SelfReferenceConstructorParameter(Config.empty)

    @Test
    fun `a non-null self reference property parameter is reported`() {
        val code = """
            class SelfRef(val ref: SelfRef)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-null self reference with a fully qualified type is reported`() {
        val code = """
            package a.b.c

            class SelfRef(val ref: a.b.c.SelfRef)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-null self reference plain parameter is reported`() {
        val code = """
            class SelfRef(name: String, ref: SelfRef)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-null self reference of a generic class is reported`() {
        val code = """
            class Node<T>(val value: T, val next: Node<T>)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-null self reference through a type alias is reported`() {
        val code = """
            typealias Ref = SelfRef

            class SelfRef(val ref: Ref)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a nullable self reference passes`() {
        val code = """
            class SelfRef(val ref: SelfRef?)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a vararg self reference passes`() {
        val code = """
            class Foo(vararg children: Foo)

            fun test() {
                Foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parameter of another class type passes`() {
        val code = """
            class Other

            class SelfRef(val ref: Other)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a self reference in a secondary constructor passes`() {
        val code = """
            class SelfRef {
                constructor(ref: SelfRef)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
