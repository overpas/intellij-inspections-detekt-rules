package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MainFunctionReturnUnitTest {

    private val environment = createEnvironment()

    private val sut = MainFunctionReturnUnit(Config.empty)

    @Test
    fun `a parameterless main function that returns String is reported`() {
        val code = """
            fun main(): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with an array parameter that returns Int is reported`() {
        val code = """
            fun main(args: Array<String>): Int {
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with an inferred Int return type is reported`() {
        val code = """
            fun main(args: Array<String>) = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with a vararg parameter that returns Int is reported`() {
        val code = """
            fun main(vararg args: String): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with a nullable array parameter that returns a nullable Int is reported`() {
        val code = """
            fun main(args: Array<String>?): Int? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension main function that returns an array is reported`() {
        val code = """
            fun Array<String>.main(): Array<String> = this
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with an annotated return type is reported`() {
        val code = """
            fun main(): @Anno String = ""

            @Target(AnnotationTarget.TYPE)
            annotation class Anno
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a main function with a JvmName annotation that returns Int is reported`() {
        val code = """
            @JvmName("main")
            fun start(args: Array<String>): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a JvmStatic main function in an object that returns Int is reported`() {
        val code = """
            object Foo {
                @JvmStatic
                fun main(args: Array<String>): Int = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a JvmStatic main function in a companion object that returns Int is reported`() {
        val code = """
            class Foo {
                companion object Bar {
                    @JvmStatic
                    fun main(args: Array<String>): Int = 1
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a JvmStatic main function in a nested object that returns String is reported`() {
        val code = """
            object Foo {
                object Bar {
                    @JvmStatic
                    fun main(args: Array<String>): String = ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `main functions that return Unit pass`() {
        val code = """
            fun main(args: Array<String>) {}

            object Foo {
                @JvmStatic
                fun main(vararg args: String) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a main function with a list parameter passes`() {
        val code = """
            fun main(args: List<String>): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function with another name passes`() {
        val code = """
            fun moin(args: Array<String>): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a main function with two parameters passes`() {
        val code = """
            fun main(args: Array<String>, flag: Boolean): String = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a main function with another JVM name passes`() {
        val code = """
            @JvmName("start")
            fun main(args: Array<String>): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a main function in a class passes`() {
        val code = """
            class Foo {
                fun main(args: Array<String>): Int = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a main function in an object without JvmStatic passes`() {
        val code = """
            object Foo {
                fun main(args: Array<String>): Int = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parameterless JvmStatic main function in an object passes`() {
        val code = """
            object Foo {
                @JvmStatic
                fun main(): Int = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a parameterless main function next to a main function with parameters passes`() {
        val code = """
            fun main(): Int = 1

            fun main(args: Array<String>) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
