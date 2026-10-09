package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantLabelTest {

    private val environment = createEnvironment()

    private val sut = RedundantLabel(Config.empty)

    @Test
    fun `a label on a local variable declaration is reported`() {
        val code = """
            fun test() {
                L@ val fn = {}
                fn()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on a higher-order function call is reported`() {
        val code = """
            fun test() {
                L@ run {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on an invoked lambda is reported`() {
        val code = """
            fun test() = l@ { 42 }()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on an object literal is reported`() {
        val code = """
            fun test() =
                L@ object {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on a parenthesized argument is reported`() {
        val code = """
            fun test() {
                bar(11, l@(todo()), "")
            }

            fun todo(): Nothing = throw Exception()

            fun bar(i: Int, s: String, a: Any) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on a safe call is reported`() {
        val code = """
            fun f(s: String?): Boolean {
                return foo@(s?.equals("a"))!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on a property delegate is reported`() {
        val code = $$"""
            import kotlin.reflect.KProperty

            class A {
                val a: String by l@ MyProperty()

                class MyProperty<T>

                operator fun <T> MyProperty<T>.getValue(thisRef: Any?, desc: KProperty<*>): T {
                    throw Exception("$thisRef $desc")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a label on a lambda passes`() {
        val code = """
            fun test() = l@ { 42 }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a label on a parenthesized lambda passes`() {
        val code = """
            fun test() = (l@ { 42 })()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a label on an annotated lambda passes`() {
        val code = """
            @Target(AnnotationTarget.EXPRESSION)
            @Retention(AnnotationRetention.SOURCE)
            annotation class Ann

            fun test() = lambda@ @Ann {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a label on an anonymous function passes`() {
        val code = """
            fun test() = l@ fun() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `labels on loops pass`() {
        val code = """
            fun test(xs: List<Any>) {
                A@ for (x in xs) {}
                B@ while (xs.isEmpty()) {}
                C@ do {} while (xs.isEmpty())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
