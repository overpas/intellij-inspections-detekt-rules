package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ExplicitThisTest {

    private val environment = createEnvironment()

    private val sut = ExplicitThis(Config.empty)

    @Test
    fun `this before a property access is reported`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    this.s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this before a member function call is reported`() {
        val code = """
            class Foo {
                fun s() = ""

                fun test() {
                    this.s()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this before an extension function call is reported`() {
        val code = """
            class Foo {
                fun test() {
                    this.s()
                }
            }

            fun Foo.s() = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this in an extension function is reported`() {
        val code = """
            fun Int.foo(): Int {
                return this.times(3)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled this to an outer receiver is reported`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    "".apply {
                        this@Foo.s
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this before a function call shadowed by a local variable is reported`() {
        val code = """
            class Foo {
                fun foo() {
                    val a = 1
                    this.a()
                }

                fun a() {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this in a property reference is reported`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    this::s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this in a function reference is reported`() {
        val code = """
            class Foo {
                fun s(x: Int) = ""

                fun test() {
                    this::s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `this before a property shadowed by a local variable passes`() {
        val code = """
            class Foo {
                var s = ""

                fun test() {
                    val s = ""
                    this.s = s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `this before a function call shadowed by an invocable variable passes`() {
        val code = """
            class Some {
                operator fun invoke() {}
            }

            class Other {
                fun foo() {
                    val a = Some()
                    this.a()
                    a()
                }

                fun a() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled this to an outer receiver of a different type with the same member passes`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    Bar().apply {
                        this@Foo.s
                    }
                }
            }

            class Bar {
                val s = ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled this to an outer receiver of the same type passes`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    Foo().apply {
                        this@Foo.s
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled this to an outer extension receiver of another type passes`() {
        val code = """
            class Foo {
                fun test() {
                    Bar().apply {
                        "".run {
                            this@apply.s()
                        }
                    }
                }
            }

            class Bar

            fun Foo.s() {}
            fun Bar.s() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `this before a different overload passes`() {
        val code = """
            class Foo {
                fun s(a: String) {}

                fun test() {
                    Bar().apply {
                        this@Foo.s("")
                    }
                }
            }

            class Bar {
                fun s(s: String) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `this in a property reference shadowed by a local variable passes`() {
        val code = """
            class Foo {
                val s = ""

                fun test() {
                    val s = 1
                    this::s
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
