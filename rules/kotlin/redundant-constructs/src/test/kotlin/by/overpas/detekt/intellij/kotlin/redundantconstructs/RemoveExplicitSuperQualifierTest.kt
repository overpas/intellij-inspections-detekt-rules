package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveExplicitSuperQualifierTest {

    private val environment = createEnvironment()

    private val sut = RemoveExplicitSuperQualifier(Config.empty)

    @Test
    fun `a qualifier with a single supertype is reported`() {
        val code = """
            open class B {
                open fun foo() {}
            }

            class A : B() {
                override fun foo() {
                    super<B>.foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an unambiguous method call is reported`() {
        val code = """
            open class B {
                open fun foo(p: String) {}

                fun foo(p: Int) {}
            }

            interface I {
                fun foo(p: String)
            }

            class A : B(), I {
                override fun foo(p: String) {
                    super<B>.foo("")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an unambiguous property access is reported`() {
        val code = """
            open class B {
                open val v: Int = 0
            }

            interface I {
                val v: Int
            }

            class A : B(), I {
                override val v: Int
                    get() = super<B>.v
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of a call with a smart cast argument is reported`() {
        val code = """
            open class B {
                open fun foo(p: String) {}

                fun foo(p: Int) {}
            }

            interface I {
                fun foo(p: String)
            }

            class A : B(), I {
                fun foo(p: Any) {
                    if (p is Int) {
                        super<B>.foo(p)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of a labeled super is reported`() {
        val code = """
            open class Base {
                open fun foo() {}
            }

            class A : Base() {
                override fun foo() {
                    super.foo()
                }

                inner class C {
                    fun test() {
                        super<Base>@A.foo()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualifier of an ambiguous method call passes`() {
        val code = """
            open class B {
                open fun foo(p: String) {}
            }

            interface I {
                fun foo(p: String) {}
            }

            class A : B(), I {
                override fun foo(p: String) {
                    super<B>.foo("")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualifier of an ambiguous property access passes`() {
        val code = """
            open class B {
                open val v: Int = 0
            }

            interface I {
                val v: Int
                    get() = 0
            }

            class A : B(), I {
                override val v: Int
                    get() = super<B>.v
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a super without a qualifier passes`() {
        val code = """
            open class B {
                open fun foo() {}
            }

            class A : B() {
                override fun foo() {
                    super.foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
