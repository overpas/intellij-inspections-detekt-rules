package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ImplicitThisTest {

    private val environment = createEnvironment()

    private val sut = ImplicitThis(Config.empty)

    @Test
    fun `an implicit function call on this is reported`() {
        val code = """
            class Foo {
                fun s() = ""

                fun test() {
                    s()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit property access on this is reported`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit receiver of a qualified chain is reported`() {
        val code = """
            class Foo {
                val f: Foo? = null
                val s = ""

                fun test() {
                    f?.s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a callable reference without a receiver is reported`() {
        val code = """
            class Foo {
                fun s() = ""

                fun test() {
                    ::s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property of an extension receiver called as a function is reported`() {
        val code = """
            class Foo {
                operator fun invoke() {}
            }

            class Bar {
                val foo = Foo()
            }

            fun Bar.test() {
                foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parameter of a function type with a receiver is reported`() {
        val code = """
            fun CharSequence.foo(bar: CharSequence.() -> Unit) {
                bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member of an outer receiver inside a lambda is reported`() {
        val code = """
            class Foo {
                fun s() = ""

                fun test() {
                    "".apply {
                        s()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member extension on a labeled outer lambda receiver is reported`() {
        val code = """
            class Foo {
                fun Bar.s() = ""
            }

            class Bar

            fun test() {
                Bar().apply {
                    Foo().apply apply2@{
                        s()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit this passes`() {
        val code = """
            class Your(val x: Int)

            fun Your.foo() = this.x
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an explicit this call passes`() {
        val code = """
            class Foo {
                fun s() = ""

                fun test() {
                    this.s()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a top-level function call passes`() {
        val code = """
            class Foo {
                fun test() {
                    s()
                }
            }

            fun s() = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local variable passes`() {
        val code = """
            class Foo {
                fun test(): Int {
                    val local = 1
                    return local
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member of a safe call receiver passes`() {
        val code = """
            class Foo {
                val s = ""

                fun test(other: Foo?) = other?.s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
