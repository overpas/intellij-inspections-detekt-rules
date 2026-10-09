package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class IfThenToSafeAccessTest {

    private val environment = createEnvironment()

    private val sut = IfThenToSafeAccess(Config.empty)

    @Test
    fun `a null check with a member access and a null branch is reported`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun test(): String? {
                val foo = maybeFoo()
                if (foo != null) {
                    foo.length
                } else {
                    null
                }
                return foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reversed null check without a then branch is reported`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun test(): String? {
                var foo = maybeFoo()
                if (null == foo) else foo.length
                return foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check without an else branch is reported`() {
        val code = """
            class Foo {
                operator fun invoke() {}
            }

            fun test(foo: Foo?) {
                if (foo != null) foo.invoke()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of an implicit receiver is reported`() {
        val code = """
            fun String?.foo() = if (this == null) null else isEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of a labeled receiver in parentheses is reported`() {
        val code = """
            class Some {
                fun bar() {}
            }

            fun Some?.foo() {
                if (((this@foo) != null)) {
                    bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a type check that returns the checked value is reported`() {
        val code = """
            interface Foo
            interface Bar : Foo

            data class Data(val foo: Foo)

            fun handle(data: Data, arg: Any?): List<Any?> {
                val bar = if (data.foo is Bar) data.foo else null
                val any = if (arg != null) arg else null
                return listOf(bar, any)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a null check of a class property is reported`() {
        val code = """
            class F(a: Int?) {
                val b = a
                val c = if (b != null) b.toString() else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check with the value as a call argument passes`() {
        val code = """
            fun convert(x: String, y: String) = x + y

            fun foo(a: String?, b: String): String? {
                return if (a != null) convert(a, b) else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type check with a member access passes`() {
        val code = """
            fun foo(arg: Any) = if (arg is String) arg.length else null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with a variable call passes`() {
        val code = """
            fun test(foo: (() -> Unit)?) {
                if (foo != null) foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of an unrelated outer receiver passes`() {
        val code = """
            class Foo

            class Bar {
                fun Foo?.test() {
                    if (this@Bar != null) {
                        bar()
                    }
                }

                fun bar() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a senseless null check passes`() {
        val code = """
            class Foo {
                fun bar() {}
            }

            fun test(foo: Foo) {
                if (foo != null) {
                    foo.bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of a mutable class property passes`() {
        val code = """
            class A(private var a: String?) {
                fun foo() = if (a != null) a.length else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with a non-null else branch passes`() {
        val code = """
            fun test(foo: String?): Int {
                return if (foo != null) foo.length else 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with a block of several statements passes`() {
        val code = """
            fun test(foo: String?): Int? {
                return if (foo != null) {
                    println("Hello")
                    foo.length
                } else {
                    null
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of a function result passes`() {
        val code = """
            fun maybeFoo(): String? = "foo"

            fun test(): Int? {
                return if (maybeFoo() == null) null else maybeFoo()?.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
