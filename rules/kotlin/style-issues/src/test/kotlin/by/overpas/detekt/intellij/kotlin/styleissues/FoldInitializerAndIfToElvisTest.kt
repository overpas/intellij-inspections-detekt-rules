package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class FoldInitializerAndIfToElvisTest {

    private val environment = createEnvironment()

    private val sut = FoldInitializerAndIfToElvis(Config.empty)

    @Test
    fun `a null check that returns is reported`() {
        val code = """
            fun foo(p: List<String?>): Int {
                val v = p[0]
                if (v == null) return -1
                return v.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check that breaks a loop is reported`() {
        val code = """
            fun foo(p: List<String?>) {
                for (i in 1..10) {
                    val v = p[i]
                    if (v == null) break
                    println(v.length)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check that continues a loop is reported`() {
        val code = """
            fun foo(p: List<String?>) {
                for (i in 1..10) {
                    val v = p[i]
                    if (null == v) continue
                    println(v.length)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check that throws in a block with a comment is reported`() {
        val code = """
            fun foo(p: List<String?>): Int {
                val v = p[0]
                if (v == null) {
                    // throw if null
                    throw RuntimeException()
                }
                return v.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check of a var with an explicit type is reported`() {
        val code = """
            fun foo(): String? = null

            fun bar() {
                var v: String? = foo()
                if (v == null) throw Exception()
                v = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated type check of the same type is reported`() {
        val code = """
            fun foo(arg: Any?) {
                val n = arg
                if (n !is Int) return
                println(n + 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated type check of a subtype is reported`() {
        val code = """
            open class A

            open class B : A() {
                fun b() {}
            }

            open class C : B() {
                fun c() {}
            }

            fun test() {
                val b: B = B()
                if (b !is C) {
                    return
                }
                b.c()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a null check that does not exit passes`() {
        val code = """
            fun foo(p: List<String?>): Int? {
                val v = p[0]
                if (v == null) bar()
                return v?.length
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a not-null check passes`() {
        val code = """
            fun foo(p: List<String?>): Int {
                val v = p[0]
                if (v != null) return -1
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with several statements passes`() {
        val code = """
            fun foo(p: List<String?>) {
                val v = p[0]
                if (v == null) {
                    bar()
                    return
                }
            }

            fun bar() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check with an else branch passes`() {
        val code = """
            fun foo(p: List<String?>): Int {
                val v = p[0]
                if (v == null) return -1 else println(v)
                return v.length
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of another variable passes`() {
        val code = """
            class C {
                var x: String? = null

                fun foo(p: List<String?>): Int {
                    val v = p[0]
                    if (x == null) return -1
                    return v!!.length
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a null check of a property with the same name passes`() {
        val code = """
            class C {
                var v: String? = null

                fun foo(p: List<String?>): Int {
                    val v = p[0]
                    if (this.v == null) return -1
                    return v!!.length
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an initializer with an elvis passes`() {
        val code = """
            fun foo(): Boolean {
                val v = bar() ?: return false
                if (v !is String) return false
                return v == ""
            }

            fun bar(): Any? = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an initializer with an if passes`() {
        val code = """
            fun test(a: String?, b: String): String {
                val x = if (a != null) a else b
                if (x == null) throw Exception()
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a multiline initializer passes`() {
        val code = """
            fun foo(vararg args: String): String? = null

            fun test(): Int {
                val foo = foo(
                    "1111111111",
                    "2222222222",
                )
                if (foo == null) return 0
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated type check of a supertype passes`() {
        val code = """
            open class A

            open class B : A() {
                fun b() {}
            }

            fun test() {
                val b = B()
                if (b !is A) {
                    return
                }
                b.b()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated type check of an unrelated interface passes`() {
        val code = """
            interface A

            open class B : A {
                fun b() {}
            }

            interface C : A {
                fun c() {}
            }

            fun test() {
                val b = B()
                if (b !is C) {
                    return
                }
                b.c()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated type check of a nullable type passes`() {
        val code = """
            fun test(): String {
                val foo = foo()
                if (foo !is String?) return "0"
                return "1"
            }

            fun foo(): Any? = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exit statement that uses the variable passes`() {
        val code = $$"""
            interface A {
                val s: String
            }

            fun foo(): Any = Any()

            fun test(): String {
                val y = foo()
                if (y !is A) return "Expected A: $y"
                return y.s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
