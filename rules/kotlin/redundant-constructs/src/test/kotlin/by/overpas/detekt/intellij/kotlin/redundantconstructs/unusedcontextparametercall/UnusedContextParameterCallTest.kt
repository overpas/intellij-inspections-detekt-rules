package by.overpas.detekt.intellij.kotlin.redundantconstructs.unusedcontextparametercall

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedContextParameterCallTest {

    private val environment = createEnvironment()

    private val sut = UnusedContextParameterCall(Config.empty)

    @Test
    fun `a context call with an unused argument is reported`() {
        val code = """
            fun foo() {}

            fun test() {
                context("") {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call with several unused arguments is reported`() {
        val code = """
            fun foo() {}

            fun test() {
                context("", 42) {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call with a partially unused argument is reported`() {
        val code = """
            context(i: Int) fun usesInt() {}

            fun test() {
                context("", 42, 1.0) {
                    usesInt()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call as a function argument is reported`() {
        val code = """
            fun produce(): Int = 1
            fun consume(x: Int) {}

            fun test() {
                consume(context("") { produce() })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call with a local variable argument is reported`() {
        val code = """
            fun foo() {}

            fun test() {
                val s = "hello"
                context(s) {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an outer context call shadowed by an inner one is reported`() {
        val code = """
            context(s: String) fun usesString() {}

            fun test() {
                context("outer") {
                    context("inner") {
                        usesString()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call with a labeled return is reported`() {
        val code = """
            fun side() {}
            fun produce(): Int = 1

            fun test() {
                val r = context("") {
                    side()
                    return@context produce()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call that passes an enclosing context parameter is reported`() {
        val code = """
            context(p: Int) fun f() {}

            context(p: Int)
            fun g() {
                context(p) {
                    f()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call that passes an enclosing context parameter of a subtype is reported`() {
        val code = """
            open class A

            class B : A()

            context(p: A) fun f() {}

            context(p: B)
            fun g() {
                context(p) {
                    f()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call that passes an enclosing context parameter among other arguments is reported`() {
        val code = """
            interface A
            interface B { val name: String }
            interface C

            class Both(override val name: String) : A, B

            context(a: A)
            fun useA() {}

            context(b: B)
            fun useB(): String = b.name

            context(c: C)
            fun useC() {}

            context(value: Both)
            fun example(c: C) {
                context(value, c) {
                    useA()
                    useB()
                    useC()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a context call whose argument is used passes`() {
        val code = """
            context(s: String) fun usesString() {}

            fun test() {
                context("") {
                    usesString()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a context call whose argument is used in a nested lambda passes`() {
        val code = """
            context(s: String) fun usesString() {}
            fun runIt(block: () -> Unit) = block()

            fun test() {
                context("") {
                    runIt { usesString() }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a context call whose argument is used by a property passes`() {
        val code = """
            context(s: String) val prop: Int get() = 0

            fun test() {
                context("") {
                    prop
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a context call whose argument is used by a contextual member passes`() {
        val code = """
            class A<T>(val a: T) {
                context(s: String)
                fun usesString(): T {
                    println(s)
                    return a
                }
            }

            fun foo2(block: A<String>.() -> Unit) {
                val a = A("JetBrains")
                a.block()
            }

            fun usesChar() {
                context("hi") {
                    foo2 { usesString() }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a context call with a side effect argument passes`() {
        val code = """
            fun makeString(): String = ""
            fun foo() {}

            fun test() {
                context(makeString()) {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a context call that passes an enclosing context parameter hidden by a closer one passes`() {
        val code = """
            open class A

            class B : A()

            context(p: A) fun f() {}

            context(p: B)
            fun g() {
                context(A()) {
                    context(p) {
                        f()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a user defined context function passes`() {
        val code = """
            fun context(x: String, block: () -> Unit) {}
            fun foo() {}

            fun test() {
                context("") {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
