package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MoveVariableDeclarationIntoWhenTest {

    private val environment = createEnvironment()

    private val sut = MoveVariableDeclarationIntoWhen(Config.empty)

    @Test
    fun `a variable used in the subject and a branch is reported`() {
        val code = """
            fun test() = 42

            fun foo() {
                val a = test()
                when (a) {
                    1 -> a
                    else -> 24
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable before a when in a property initializer is reported`() {
        val code = """
            fun test() = 42

            fun foo(): Int {
                val a = test()
                val b = when (a) {
                    1 -> a
                    else -> 24
                }
                return b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable before a returned when is reported`() {
        val code = """
            fun test() = 42

            fun foo(): Int {
                val a = test()
                return when (a) {
                    1 -> a
                    else -> 24
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable with a trailing comment is reported`() {
        val code = """
            fun foo(style: Int): Int {
                val a = style // comment
                return when (a) {
                    0 -> 0
                    else -> a
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable whose moved declaration fits the right margin is reported`() {
        val code = """
            fun test() {
                val foo = 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1
                when (foo) {
                    1 -> foo
                    else -> 24
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable whose moved declaration exceeds the right margin passes`() {
        val code = """
            fun test() {
                val foo = 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1
                when (foo) {
                    1 -> foo
                    else -> 24
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable used only in the subject passes`() {
        val code = """
            fun foo() {
                val a = 1
                when (a) {
                    1 -> {
                    }
                    else -> {
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable with an elvis return in the initializer passes`() {
        val code = """
            fun test(str: String?): String? {
                val some = str ?: return null
                return when (some) {
                    "some" -> some
                    else -> ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable with an if initializer passes`() {
        val code = """
            fun test(str: String?): String? {
                val some = if (str != null) str + str else throw Exception()
                return when (some) {
                    "some" -> some
                    else -> ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable with a lambda in the initializer passes`() {
        val code = """
            fun foo(): Int {
                val a = listOf(1).filter { it > 0 }.max()
                return when (a) {
                    1 -> a
                    else -> 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable declared on several lines passes`() {
        val code = """
            fun test() = 42

            fun foo() {
                val a =
                    test()
                when (a) {
                    1 -> a
                    else -> 24
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable that is not right before the when passes`() {
        val code = """
            fun foo() {
                val a = 1
                val b = 42
                when (a) {
                    1 -> a + b
                    else -> 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a variable used after the when passes`() {
        val code = """
            fun foo(): Int {
                val a = 1
                when (a) {
                    1 -> a
                    else -> 0
                }
                return a
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var passes`() {
        val code = """
            fun foo() {
                var a = 1
                when (a) {
                    1 -> a
                    else -> 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when on the right side of an elvis passes`() {
        val code = """
            fun test() = true

            fun foo(): Int {
                val a = test()
                return null ?: when (a) {
                    true -> if (a) 42 else 0
                    else -> 5
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
