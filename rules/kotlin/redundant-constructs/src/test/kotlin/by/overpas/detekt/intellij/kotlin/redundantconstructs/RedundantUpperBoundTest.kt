package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantUpperBoundTest {

    private val environment = createEnvironment()

    private val sut = RedundantUpperBound(Config.empty)

    @Test
    fun `a nullable Any bound of a function type parameter is reported`() {
        val code = """
            fun <T : Any?> foo(t: T) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a nullable Any bound of a class type parameter is reported`() {
        val code = """
            class C<out T : Any?>
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a nullable Any bound through a type alias is reported`() {
        val code = """
            typealias Anything = Any?

            fun <T : Anything> foo(t: T) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a non-null Any bound passes`() {
        val code = """
            fun <T : Any> foo(t: T) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a nullable bound of another type passes`() {
        val code = """
            fun <T : CharSequence?> foo(t: T) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
