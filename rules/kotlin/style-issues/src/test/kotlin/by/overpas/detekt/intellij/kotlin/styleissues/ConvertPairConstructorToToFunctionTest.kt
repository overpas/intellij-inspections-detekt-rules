package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertPairConstructorToToFunctionTest {

    private val environment = createEnvironment()

    private val sut = ConvertPairConstructorToToFunction(Config.empty)

    @Test
    fun `a Pair constructor call is reported`() {
        val code = """
            fun test() {
                val p = Pair(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Pair constructor call with an explicit import is reported`() {
        val code = """
            import kotlin.Pair

            fun test() {
                val p = Pair(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Pair constructor call with explicit type arguments is reported`() {
        val code = """
            fun test() {
                val p = Pair<Int, String>(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified Pair constructor call is reported`() {
        val code = """
            fun test() {
                val p = kotlin.Pair(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Pair constructor call with named arguments is reported`() {
        val code = """
            fun test() {
                val p = Pair(second = "foo", first = 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a to call passes`() {
        val code = """
            fun test() {
                val p = 1 to "foo"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a constructor call of a user-defined Pair class passes`() {
        val code = """
            class Pair(val first: Int, val second: String)

            fun test() {
                val p = Pair(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call of a function named Pair passes`() {
        val code = """
            fun Pair(first: Int, second: String): String = second + first

            fun test() {
                val p = Pair(1, "foo")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a Triple constructor call passes`() {
        val code = """
            fun test() {
                val t = Triple(1, "foo", 2.0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
