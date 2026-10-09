package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class LazyWithoutDelegationTest {

    private val environment = createEnvironment()

    private val sut = LazyWithoutDelegation(Config.empty)

    @Test
    fun `a local lazy value read through value is reported`() {
        val code = """
            class A {
                fun test() {
                    val x = lazy { "hello" }
                    println(x.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a private top-level lazy value read through value is reported`() {
        val code = """
            private val x = lazy { "hello" }

            fun test() {
                println(x.value)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a private lazy property read through value several times is reported`() {
        val code = """
            class A {
                private val x = lazy { "hello" }

                fun test() {
                    val length = x.value.length
                    println(x.value + length)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lazy value read through value in parentheses is reported`() {
        val code = """
            class A {
                fun test() {
                    val x = lazy { "hello" }
                    println((x).value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lazy property read through value with a this receiver is reported`() {
        val code = """
            class A {
                private val p = lazy { "hello" }

                fun test() {
                    println(this.p.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lazy property initialized by a qualified call is reported`() {
        val code = """
            class A {
                fun computeLazily(x: Int) = lazy { x.toString() }
            }

            class B {
                private val p = A().computeLazily(5)

                fun test() {
                    println(p.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused private lazy property initialized by a function call is reported`() {
        val code = """
            class A {
                fun foo(x: Int) {}

                fun computeLazily(x: Int) = lazy { foo(x) }

                private val p = computeLazily(5)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a lazy property that is checked with isInitialized passes`() {
        val code = """
            class A {
                private val p = lazy { "hello" }

                fun test() {
                    if (p.isInitialized()) {
                        println(p.value)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lazy property that is passed as an argument passes`() {
        val code = """
            class A {
                private val x = lazy { "hello" }

                fun takeLazy(lazyValue: Lazy<String>) {}

                fun test() {
                    takeLazy(lazyValue = x)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lazy property that is a receiver of an if expression passes`() {
        val code = """
            class A {
                private val x = lazy { "hello" }

                fun test(flag: Boolean, y: Lazy<String>) {
                    println((if (flag) x else y).value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an annotated lazy property passes`() {
        val code = """
            annotation class Ann

            class A {
                @Ann
                private val x = lazy { "hello" }

                fun test() {
                    println(x.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a public lazy property passes`() {
        val code = """
            class A {
                val x = lazy { "hello" }

                fun test() {
                    println(x.value)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lazy property with an explicit type passes`() {
        val code = """
            private val x: Lazy<String> = lazy { "hello" }

            fun test() {
                println(x.value)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a private lazy var passes`() {
        val code = """
            private var foo = lazy { "hello" }

            fun test() {
                println(foo.value)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a delegated lazy property passes`() {
        val code = """
            private val foo by lazy { "hello" }

            fun test() {
                println(foo)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
