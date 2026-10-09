package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithCallWithContextCallTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithCallWithContextCall(Config.empty)

    @Test
    fun `a with call whose receiver is only a context argument is reported`() {
        val code = """
            context(x: String)
            fun foo1(count: Int): String {
                return x.repeat(count)
            }

            fun main() {
                with("string") { foo1(2) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call whose receiver is a context argument of two calls is reported`() {
        val code = """
            context(x: String)
            fun length(): Int = x.length

            context(x: String)
            fun repeated(n: Int): String = x.repeat(n)

            fun main() {
                with("hi") {
                    length()
                    repeated(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with a labeled return is reported`() {
        val code = """
            context(x: String)
            fun foo1(): String = x

            fun main() {
                with("hi") {
                    if (foo1().isEmpty()) return@with
                    println("not empty")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call whose receiver is passed to a call with a lambda is reported`() {
        val code = """
            context(x: String)
            fun withLen(block: (Int) -> Unit) {
                block(x.length)
            }

            fun main() {
                with("hi") {
                    withLen { println(it) }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with a non-trailing lambda is reported`() {
        val code = """
            class Ctx

            context(c: Ctx)
            fun fdemo() {
            }

            fun testCtx2() {
                with(Ctx(), {
                    fdemo()
                })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call whose receiver is a dispatch receiver passes`() {
        val code = """
            fun main() {
                with(StringBuilder()) {
                    append("hi")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call whose receiver is an extension receiver passes`() {
        val code = """
            fun String.shout() = uppercase()

            fun main() {
                with("hi") {
                    shout()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses this passes`() {
        val code = """
            context(x: String)
            fun foo1(count: Int): String = x.repeat(count)

            fun main() {
                with("hi") {
                    val copy = this
                    foo1(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses a labeled this passes`() {
        val code = """
            context(x: String)
            fun foo1(count: Int): String = x.repeat(count)

            fun main() {
                with("hi") {
                    println(this@with)
                    foo1(2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call with a named receiver argument passes`() {
        val code = """
            context(x: String)
            fun foo1(count: Int): String = x.repeat(count)

            fun main() {
                with(receiver = "hi") { foo1(2) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified with call passes`() {
        val code = """
            object Foo {
                fun with(x: String, block: String.() -> Unit) = x.block()
            }

            fun main() {
                Foo.with("hi") { println(this) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call whose receiver is not used passes`() {
        val code = """
            fun main() {
                with("hi") {
                    println("not used")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a custom with call with a spread argument passes`() {
        val code = """
            context(x: String)
            fun foo1(count: Int): String = x.repeat(count)

            fun with(vararg values: String, block: String.() -> Unit) {
                values.first().block()
            }

            fun main() {
                val args = arrayOf("hi")
                with(*args) { foo1(2) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
