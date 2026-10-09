package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class IfThenToElvisTest {

    private val environment = createEnvironment()

    private val sut = IfThenToElvis(Config.empty)

    @Test
    fun `a null check that returns the value or a fallback is reported`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun main() {
                val foo = maybeFoo()
                val bar = "bar"
                val x = if (foo == null) {
                    bar
                } else {
                    foo
                }
                val y = "baz" + if (null != foo) foo else bar
                println(x + y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a null check of a stable local variable is reported`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun test(): String? {
                var foo = maybeFoo()
                val bar = if (foo == null) "hello" else foo
                return foo + bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check with a member access is reported`() {
        val code = """
            class My(val x: Int)

            fun foo(arg: My?): Int {
                return if (arg != null) arg.x else 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check with a fallback comparison is reported`() {
        val code = """
            class My(val local: Boolean)

            class Your(val my: My?, val parent: Any?)

            fun foo(your: Your): Boolean {
                val my = your.my
                return if (my != null) my.local else your.parent != null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type check that returns the checked value is reported`() {
        val code = """
            class My(val x: Int)

            fun foo(arg: Any?): My {
                return if (arg !is My) My(42) else arg
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of an implicit receiver is reported`() {
        val code = """
            fun String?.foo() = if (this == null) true else isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check that throws an exception with a message is reported`() {
        val code = """
            fun test(t: String?): String {
                return if (t != null) t else throw NullPointerException("'t' must not be null")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check used as a statement passes`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun main() {
                val foo = maybeFoo()
                val bar = "bar"
                if (foo != null) foo else bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check that passes the value to a call passes`() {
        val code = """
            fun bar(s: String): Int = s.length

            fun foo(s: String?): Int {
                return if (s != null) bar(s) else 13
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type check with a member access passes`() {
        val code = """
            class My(val x: Int)

            fun foo(arg: Any?): Int {
                return if (arg is My) arg.x else 42
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check that throws a plain null pointer exception passes`() {
        val code = """
            fun test(t: String?): String {
                return if (t == null) throw NullPointerException() else t
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with a nullable result passes`() {
        val code = """
            fun String.bar(): String? = null

            fun foo(p: String?): String? {
                return if (p != null) p.bar() else "a"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type check for an unrelated type passes`() {
        val code = """
            interface A
            interface B

            fun prepare(x: A) = x

            fun test(x: A): A {
                return if (x is B) x else prepare(x)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of a captured variable passes`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun capture(block: () -> Unit) = block()

            fun test(): Any? {
                var foo = maybeFoo()
                capture {
                    foo = null
                }
                val bar = if (foo == null) 42 else foo
                return bar
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with a null fallback passes`() {
        val code = """
            fun test(foo: String?): String? {
                return if (foo == null) null else foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
