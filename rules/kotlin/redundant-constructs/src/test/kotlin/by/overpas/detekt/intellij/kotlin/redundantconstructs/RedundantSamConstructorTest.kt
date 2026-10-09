package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantSamConstructorTest {

    private val environment = createEnvironment()

    private val sut = RedundantSamConstructor(Config.empty)

    @Test
    fun `a Java SAM constructor passed as an argument is reported`() {
        val code = """
            fun usage(r: Runnable) {}

            fun test() {
                usage(Runnable { })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a Kotlin fun interface constructor passed as an argument is reported`() {
        val code = """
            fun interface KtRunnable {
                fun run()
            }

            class Test {
                fun usage(r: KtRunnable) {}

                fun test() {
                    usage(KtRunnable { })
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a SAM constructor next to another argument is reported`() {
        val code = """
            fun test(r1: Runnable, r2: Runnable) {}

            fun usage(r1: Runnable) {
                test(r1, Runnable {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified SAM constructor of a nested interface is reported`() {
        val code = """
            object Foo {
                fun interface Bar {
                    fun baz()
                }

                fun foo(bar: Bar) {
                    bar.baz()
                }
            }

            fun test() {
                Foo.foo(Foo.Bar { })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `several SAM constructors in one call are reported once`() {
        val code = """
            fun test(r1: Runnable, r2: Runnable) {}

            fun usage() {
                test(Runnable {}, Runnable {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `only the SAM constructor without a labeled return is reported`() {
        val code = """
            fun test(r1: Runnable, r2: Runnable) {}

            fun usage() {
                test(Runnable { return@Runnable }, Runnable {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a SAM constructor for a generic parameter passes`() {
        val code = """
            fun <T> test(t: T): T = t

            fun usage() {
                test(Runnable {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a SAM constructor of a subinterface passes`() {
        val code = """
            fun interface Base {
                fun test1()
            }

            fun interface Extender : Base {
                override fun test1() {
                    test2()
                }

                fun test2()
            }

            fun take(b: Base) {}

            fun usage() {
                take(Extender {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a SAM constructor with a labeled return passes`() {
        val code = """
            fun test(r: Runnable) {}

            fun usage() {
                test(Runnable { return@Runnable })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a SAM constructor with a labeled lambda passes`() {
        val code = """
            fun test(r: Runnable) {}

            fun usage() {
                test(Runnable a@{ return@a })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a SAM constructor that resolves an overload passes`() {
        val code = """
            fun interface FunInterface1 {
                fun test()
            }

            fun interface FunInterface2 {
                fun test(): Int
            }

            fun foo(f1: FunInterface1) {}

            fun foo(f2: FunInterface2) {}

            fun usage() {
                foo(FunInterface1 { 10 })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a SAM constructor that selects a member over a local function passes`() {
        val code = """
            fun interface FunInterface1 {
                fun test()
            }

            interface JavaTest {
                fun foo(f1: FunInterface1)
            }

            fun JavaTest.usage() {
                fun foo(a: () -> Unit) {}

                foo(FunInterface1 { 10 })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
