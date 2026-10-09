package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnnecessaryVariableTest {

    private val environment = createEnvironment()

    private val sut = UnnecessaryVariable(Config.empty)

    @Test
    fun `a copy of a local value is reported`() {
        val code = """
            fun test(): Int {
                val x = 1
                val y = x
                return x + y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy of a parameter is reported`() {
        val code = """
            fun sqr(arg: Int): Int {
                val other = arg
                return other * other
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy followed by a comment on the next line is reported`() {
        val code = """
            fun foo(x: Int) {
                val y = x
                // comment
                println(y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy of an implicit lambda parameter without a conflict is reported`() {
        val code = """
            fun main() {
                fun g(x: Long) = x
                listOf(0L).map {
                    val o = it
                    run {
                        g(o)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a copy in a when subject is reported`() {
        val code = $$"""
            fun test(value: Any): String {
                return when (val v = value) {
                    is String -> "$v is String"
                    else -> "$v"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused copy passes`() {
        val code = """
            fun test() {
                val x = 1
                val y = x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy with an explicit type passes`() {
        val code = """
            fun test(): Int {
                val x = 1
                val y: Int = x
                return x + y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy of a var passes`() {
        val code = """
            fun test(): Int {
                var x = 1
                val y = x
                x++
                return x + y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var copy passes`() {
        val code = """
            fun sqrPlusOne(arg: Int): Int {
                var other = arg
                other++
                return other * other
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy of a delegated value passes`() {
        val code = """
            fun test() {
                val a: Int? by lazy { null }
                val b = a
                if (b != null) {
                    println(b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an annotated copy passes`() {
        val code = """
            annotation class Ann

            fun foo(x: Int): Int {
                @Ann
                val y = x
                return y + y
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy with a comment before it passes`() {
        val code = """
            fun foo(x: Int) {
                // comment
                val y = x
                println(y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy with a trailing comment passes`() {
        val code = """
            fun foo(x: Int) {
                val y = x // comment
                println(y)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy of it used inside a lambda with its own it passes`() {
        val code = """
            fun foo(a: List<String>, b: List<Int>) {
                a.forEach {
                    val a2 = it
                    b.forEach {
                        println(a2.length)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy whose source name is shadowed below passes`() {
        val code = """
            interface Callback {
                fun on(id: Int)
            }

            fun called(id: Int) {
                val calledId = id
                object : Callback {
                    override fun on(id: Int) {
                        if (id == calledId) {
                            println(id)
                        }
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy of a member property passes`() {
        val code = """
            class My(val x: Number)

            fun My.foo(): Int {
                val y = x
                if (y is Int) return y
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a copy of a top level property passes`() {
        val code = """
            val x: Number? = null

            fun foo(): Int {
                val y = x
                if (y is Int) return y
                return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an overriding property in an object passes`() {
        val code = """
            abstract class Foo {
                abstract val bar: Int
            }

            fun test() {
                val i = 1
                object : Foo() {
                    override val bar = i
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
