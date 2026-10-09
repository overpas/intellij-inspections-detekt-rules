package by.overpas.detekt.intellij.kotlin.namingconventions

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class InconsistentCommentForJavaParameterTest {

    private val environment = createEnvironment(listOf(Path("src/test/resources/InconsistentCommentForJavaParameter")))

    private val sut = InconsistentCommentForJavaParameter(Config.empty)

    @Test
    fun `a comment with another parameter name is reported`() {
        val code = """
            fun foo() {
                val j = J()
                j.foo(/* numbe = */ 1, /* v = */ "a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `comments with another parameter name and any whitespace are reported`() {
        val code = """
            fun foo(j: J) {
                j.foo(/*num = */1, /* s= */"a")
                j.foo(/* num=*/1, /*  s  =  */"a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(4, findings.size)
    }

    @Test
    fun `a comment in a super type call is reported`() {
        val code = """
            class B : A(/* i = */ 4)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comment in a super constructor delegation call is reported`() {
        val code = """
            class KJ : J {
                constructor(number: Int) : super(/* numb = */ number)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comment in a Java annotation is reported`() {
        val code = """
            @MyAnnotation(/* ...val = */ "a")
            class AK
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comment without the vararg prefix for a vararg parameter is reported`() {
        val code = """
            fun foo(j: J) {
                j.bar("a", /* rest = */ "b")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comment for a parameter of a generic Java class is reported`() {
        val code = """
            fun foo(box: Box<String>) {
                box.put(/* value = */ "a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a comment before a call with a trailing lambda is reported`() {
        val code = """
            fun foo() {
                J.run(/* times = */ 1) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `comments with the parameter names and any whitespace pass`() {
        val code = """
            fun foo(j: J) {
                j.foo(/* number = */ 1, /* v = */ "a")
                j.foo(/*number = */1, /*  v  =  */"a")
                j.foo(/* number=*/1, /* v =*/"a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comment that does not end with an equals sign passes`() {
        val code = """
            fun foo(j: J) {
                j.foo(/* 1st argument */ 1, "a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `comments after the first vararg argument pass`() {
        val code = """
            @MyAnnotation(/* ...value = */ "a", /* other = */ "b")
            class AK

            fun foo(j: J) {
                j.bar(/* first = */ "a", /* ...rest = */ "b", /* other = */ "c")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a comment for a Kotlin function passes`() {
        val code = """
            fun bar(number: Int) {}

            fun foo() {
                bar(/* value = */ 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
