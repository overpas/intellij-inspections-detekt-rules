package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CopyWithoutNamedArgumentsTest {

    private val environment = createEnvironment()

    private val sut = CopyWithoutNamedArguments(Config.empty)

    @Test
    fun `a copy call with a positional argument is reported`() {
        val code = """
            data class Foo(val a: String)

            fun bar(f: Foo) {
                f.copy("")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy call with positional and named arguments is reported`() {
        val code = """
            data class SomeName(val a: Int, val b: Int, val c: String)

            fun foo(f: SomeName) {
                f.copy(2, c = "")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy call on an implicit receiver is reported`() {
        val code = """
            data class SomeName(val a: Int, val b: Int, val c: String)

            fun SomeName.func() = copy(1, 0)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy call on a safe call is reported`() {
        val code = """
            data class Foo(val a: String)

            fun bar(f: Foo?) = f?.copy("")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy call on a type alias receiver is reported`() {
        val code = """
            data class Foo(val a: String)

            typealias Bar = Foo

            fun bar(f: Bar) = f.copy("")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy call with named arguments passes`() {
        val code = """
            data class SomeName(val a: Int, val b: Int, val c: String)

            fun foo(f: SomeName) {
                f.copy(a = 0, b = 0, c = "")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy call without arguments passes`() {
        val code = """
            data class Foo(val a: String)

            fun bar(f: Foo) = f.copy()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy function of a regular class passes`() {
        val code = """
            class Foo(val a: String) {
                fun copy(a: String) = Foo(a)
            }

            fun bar(f: Foo) = f.copy("")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy extension function on a data class passes`() {
        val code = """
            data class Foo(val a: String)

            fun Foo.copy(a: Int) = Foo(a.toString())

            fun bar(f: Foo) = f.copy(1)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a top-level copy function passes`() {
        val code = """
            fun copy(a: String) = a

            fun bar() = copy("")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
