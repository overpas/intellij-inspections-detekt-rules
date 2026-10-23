package by.overpas.detekt.intellij.kotlin.styleissues.useexpressionbody

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
    fun `a one-line return is reported on the return keyword`() {
        val code = """
            fun sqr(x: Int): Int {
                return x * x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)
        val reportedText = findings.single().entity.location.text
            .run { code.substring(start, end) }

        assertEquals("return", reportedText)
    }

    @Test
    fun `a one-line return with a labeled return inside a lambda is reported`() {
        val code = """
            fun List<String>.fn(): List<String> {
                return map { if (it.isEmpty()) return@map "<empty>" else it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a one-line return of an object literal is reported`() {
        val code = """
            interface I {
                fun foo(): String
            }

            fun bar(): I {
                return object : I { override fun foo(): String = "a" }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with a one-line return is reported`() {
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
    fun `a multi-line return passes`() {
        val code = """
            fun Int?.orZero(): Int {
                return this
                    ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return of a when expression passes`() {
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

        assertEquals(0, findings.size)
    }

    @Test
    fun `a one-line return of a when expression passes`() {
        val code = """
            fun sign(x: Int): Int {
                return when { x < 0 -> -1 else -> 1 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an empty function body passes`() {
        val code = """
            fun foo() {
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single call of a Unit function passes`() {
        val code = """
            fun foo() {
                bar()
            }

            fun bar() = Unit
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a setter with a single Unit call passes`() {
        val code = """
            fun log(value: String) = Unit

            var foo: String = ""
                set(value) {
                    log(value)
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a single throw passes`() {
        val code = """
            fun foo(): Nothing {
                throw UnsupportedOperationException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with a single throw passes`() {
        val code = """
            val foo: String
                get() {
                    throw UnsupportedOperationException()
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with an else branch of type Unit passes`() {
        val code = """
            fun bar() = Unit

            fun foo(f: Boolean) {
                if (f) bar() else bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
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
    fun `a one-line return with an early return inside passes`() {
        val code = """
            fun foo(x: Int?): Int {
                return x ?: return 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
