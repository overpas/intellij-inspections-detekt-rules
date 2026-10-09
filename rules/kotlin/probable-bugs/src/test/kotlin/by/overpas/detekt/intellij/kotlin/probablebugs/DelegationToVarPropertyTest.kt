package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegationToVarPropertyTest {

    private val environment = createEnvironment()

    private val sut = DelegationToVarProperty(Config.empty)

    @Test
    fun `a delegation to a var parameter is reported`() {
        val code = """
            interface A {
                fun foo()
            }

            class B : A {
                override fun foo() {}
            }

            class C(var b: B) : A by b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegation to a var parameter that a function reads is reported`() {
        val code = """
            class Foo(var text: CharSequence) : CharSequence by text {
                fun bar() {
                    text
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegation to a var parameter that a property initializer reads is reported`() {
        val code = """
            class Foo(var text: CharSequence) : CharSequence by text {
                val bar = text
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegation of an inner class to a var parameter of the outer class is reported`() {
        val code = """
            class Foo(var text: CharSequence) {
                inner class Bar : CharSequence by text
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a delegation to a var parameter that a function assigns passes`() {
        val code = """
            class Foo(var text: CharSequence) : CharSequence by text {
                fun bar() {
                    text = ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegation to a var parameter that a function assigns through this passes`() {
        val code = """
            class Foo(var text: CharSequence) : CharSequence by text {
                fun bar() {
                    this.text = ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegation to a var parameter that a function increments passes`() {
        val code = """
            interface Counter {
                fun count(): Int
            }

            class Fixed(private val value: Int) : Counter {
                override fun count() = value
            }

            class Holder(var counter: Counter) : Counter by counter {
                var calls = 0

                fun bar() {
                    calls++
                    counter = Fixed(calls)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegation to a val parameter passes`() {
        val code = """
            interface A {
                fun foo()
            }

            class B : A {
                override fun foo() {}
            }

            class C(val b: B) : A by b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegation to a plain parameter passes`() {
        val code = """
            interface A {
                fun foo()
            }

            class B : A {
                override fun foo() {}
            }

            class C(b: B) : A by b
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegation to a new instance passes`() {
        val code = """
            interface A {
                fun foo()
            }

            class B : A {
                override fun foo() {}
            }

            class C(var b: B) : A by B()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
