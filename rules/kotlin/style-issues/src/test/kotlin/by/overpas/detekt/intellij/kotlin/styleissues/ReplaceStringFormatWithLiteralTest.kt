package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceStringFormatWithLiteralTest {

    private val environment = createEnvironment()

    private val sut = ReplaceStringFormatWithLiteral(Config.empty)

    @Test
    fun `a String format call with only string placeholders is reported`() {
        val code = """
            class Bar {
                val value = 2
            }

            fun test(): String {
                val foo = 1
                return String.format("foo is %s, bar is %s.", foo, Bar().value)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a String format call with repeated arguments is reported`() {
        val code = """
            fun main() {
                val id = "abc"
                val date = "123"
                println(String.format("%s_%s_%s", id, date, id))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a fully qualified Java String format call is reported`() {
        val code = """
            fun test(): String {
                val foo = 1
                val bar = 2
                return java.lang.String.format("foo is %s, bar is %s.", foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported Java String format call is reported`() {
        val code = """
            import java.lang.String.format

            fun test(): String {
                val foo = 1
                val bar = 2
                return format("foo is %s, bar is %s.", foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a String format call with a Formattable argument passes`() {
        val code = $$"""
            import java.util.Formattable
            import java.util.Formatter

            class Foo(private val value: Int) : Formattable {
                override fun formatTo(formatter: Formatter?, flags: Int, width: Int, precision: Int) {
                    formatter?.out()?.append("[$value]")
                }
            }

            fun test(): String {
                val foo = Foo(1)
                val bar = 2
                return String.format("foo is %s, bar is %s.", foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a String format call with fewer arguments than placeholders passes`() {
        val code = """
            fun test(): String {
                val foo = 1
                return String.format("foo is %s, bar is %s.", foo)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a String format call without arguments passes`() {
        val code = """
            fun test(): String = String.format("%%")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a String format call with a decimal placeholder passes`() {
        val code = """
            fun test(): String {
                val foo = 1
                val bar = 2
                return String.format("foo is %s, bar is %d.", foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a String format call with a line separator placeholder passes`() {
        val code = """
            fun test(): String {
                val foo = 1
                val bar = 2
                return String.format("foo is %s, bar is %s.%n", foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a String format call with a raw string passes`() {
        val code = """
            fun test(): String {
                val foo = 1
                val bar = 2
                return String.format(${"\"\"\""}foo is %s, bar is %s.${"\"\"\""}, foo, bar)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a format extension call on a string passes`() {
        val code = """
            fun test(): String {
                val foo = 1
                return "foo is %s.".format(foo)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom format function passes`() {
        val code = """
            fun format(pattern: String, vararg args: Any?): String = pattern + args.size

            fun test(): String {
                val foo = 1
                return format("foo is %s.", foo)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
