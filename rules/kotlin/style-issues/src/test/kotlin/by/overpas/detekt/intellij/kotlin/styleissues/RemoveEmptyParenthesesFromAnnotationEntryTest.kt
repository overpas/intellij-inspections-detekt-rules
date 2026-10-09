package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveEmptyParenthesesFromAnnotationEntryTest {

    private val environment = createEnvironment(
        listOf(Path("src/test/resources/RemoveEmptyParenthesesFromAnnotationEntry")),
    )

    private val sut = RemoveEmptyParenthesesFromAnnotationEntry(Config.empty)

    @Test
    fun `empty parentheses after an annotation without parameters are reported`() {
        val code = """
            annotation class MyAnnotation

            @MyAnnotation()
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `empty parentheses after an annotation whose parameters all have defaults are reported`() {
        val code = """
            annotation class MyAnnotation(val x: Int = 10)

            @MyAnnotation()
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `empty parentheses with whitespace inside are reported`() {
        val code = """
            annotation class MyAnnotation

            @MyAnnotation(
            )
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `empty parentheses after an annotation on an expression are reported`() {
        val code = """
            @Target(AnnotationTarget.EXPRESSION)
            @Retention(AnnotationRetention.SOURCE)
            annotation class MyAnnotation

            fun test() {
                val x = @MyAnnotation() 5
                println(x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `empty parentheses after a Java annotation are reported`() {
        val code = """
            @MyJavaAnnotation()
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an annotation without parentheses passes`() {
        val code = """
            annotation class MyAnnotation

            @MyAnnotation
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an annotation with arguments passes`() {
        val code = """
            annotation class MyAnnotation(val x: Int)

            @MyAnnotation(1)
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `empty parentheses after an annotation with a vararg parameter pass`() {
        val code = """
            annotation class MyAnnotation(vararg val names: String)

            @MyAnnotation()
            fun test() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
