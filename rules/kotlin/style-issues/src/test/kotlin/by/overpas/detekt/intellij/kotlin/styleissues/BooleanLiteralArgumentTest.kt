package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class BooleanLiteralArgumentTest {

    private val environment = createEnvironment(listOf(Path("src/test/resources/BooleanLiteralArgument")))

    private val sut = BooleanLiteralArgument(Config.empty)

    @Test
    fun `three adjacent boolean literal arguments are reported`() {
        val code = """
            fun foo(a: Boolean, b: Boolean, c: Boolean) {}

            fun test() {
                foo(true, true, true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `two adjacent boolean literal arguments before a named argument are reported`() {
        val code = """
            fun foo(a: Boolean, b: Boolean, c: Boolean) {}

            fun test() {
                foo(true, false, c = true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `adjacent boolean literal arguments of a constructor call are reported`() {
        val code = """
            class Options(val verbose: Boolean, val strict: Boolean)

            val options = Options(true, false)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a boolean literal argument next to a boolean variable passes`() {
        val code = """
            fun foo(a: Boolean, b: Boolean, c: Boolean) {}

            fun test(b: Boolean) {
                foo(b, b, true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single boolean literal argument passes`() {
        val code = """
            fun foo(a: Boolean, b: Int, c: Boolean) {}

            fun test() {
                foo(true, 0, true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `named boolean literal arguments pass`() {
        val code = """
            fun foo(a: Boolean, b: Boolean, c: Boolean) {}

            fun test() {
                foo(a = true, b = true, c = true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `boolean literal arguments of a vararg parameter pass`() {
        val code = """
            fun foo(vararg b: Boolean) {}

            fun test() {
                foo(true, true, true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a boolean literal argument next to boolean vararg arguments passes`() {
        val code = """
            fun foo(a: Boolean, vararg b: Boolean) {}

            fun test() {
                foo(true, true, true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `boolean literal arguments of a Pair or a Triple pass`() {
        val code = """
            val pair = Pair(true, false)
            val triple = Triple(true, false, true)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `boolean literal arguments of a Java method pass`() {
        val code = """
            fun test() {
                JavaClass().foo(true, false)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
