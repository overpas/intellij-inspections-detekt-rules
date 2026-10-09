package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UseExpressionBodyTest {

    private val environment = createEnvironment()

    private val sut = UseExpressionBody(Config.empty)

    @Test
    fun `a one-line return is reported`() {
        val code = """
            fun sqr(x: Int): Int {
                return x * x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a multi-line return is reported`() {
        val code = """
            fun Int?.orZero(): Int {
                return this
                    ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return of a when expression is reported`() {
        val code = """
            fun sign(x: Int): Int {
                return when {
                    x < 0 -> -1
                    x > 0 -> 1
                    else -> 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return with a labeled return inside a lambda is reported`() {
        val code = """
            fun List<String>.fn(): List<String> {
                return map {
                    if (it.isEmpty()) return@map "<empty>"
                    it
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return of an object literal is reported`() {
        val code = """
            interface I {
                fun foo(): String
            }

            fun bar(): I {
                return object : I {
                    override fun foo(): String = "a"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with a return is reported`() {
        val code = """
            val foo: String
                get() {
                    return "abc"
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an empty function body is reported`() {
        val code = """
            fun foo() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single call of a Unit function is reported`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar() = Unit
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a single throw is reported`() {
        val code = """
            fun foo(): Nothing {
                throw UnsupportedOperationException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if with an else branch of type Unit is reported`() {
        val code = """
            fun bar() = Unit

            fun foo(f: Boolean) {
                if (f) bar() else bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an exhaustive when of type Unit is reported`() {
        val code = """
            enum class AccessMode { READ, WRITE, RW }

            fun whenExpr(access: AccessMode) {
                when (access) {
                    AccessMode.READ -> println("read")
                    AccessMode.WRITE -> println("write")
                    AccessMode.RW -> println("rw")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with an expression body passes`() {
        val code = """
            fun foo() = "abc"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a body with several statements passes`() {
        val code = $$"""
            fun foo(): String {
                val v = 1
                return "abc$v"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return without a value passes`() {
        val code = """
            fun foo() {
                return
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single assignment passes`() {
        val code = """
            var a = 1
            var b = 2

            fun foo() {
                a = b
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single declaration passes`() {
        val code = """
            fun foo() {
                val v = 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single loop passes`() {
        val code = """
            fun foo(): String {
                while (true) { }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single call of a function that returns a value passes`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar(): String = "abc"
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if without an else branch passes`() {
        val code = """
            fun bar() = Unit

            fun foo(f: Boolean) {
                if (f) bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-exhaustive when passes`() {
        val code = """
            fun foo(x: Int) {
                when (x) {
                    1 -> println("one")
                    2 -> println("two")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when with an if without an else branch passes`() {
        val code = """
            enum class AccessMode { READ, WRITE, RW }

            fun whenExpr(mode: Boolean, access: AccessMode) {
                when (access) {
                    AccessMode.READ -> if (mode) println("read") else println("noread")
                    AccessMode.WRITE -> if (mode) println("write")
                    AccessMode.RW -> if (mode) println("both") else println("no both")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return with an early return inside passes`() {
        val code = """
            fun sign(x: Int): Int {
                return when {
                    x < 0 -> -1
                    x > 0 -> if (x == 42) return 42 else 1
                    else -> 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with an assignment passes`() {
        val code = """
            var foo: String = ""
                set(value) {
                    field = value
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a secondary constructor with an empty body passes`() {
        val code = """
            class C {
                constructor() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
