package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantWithTest {

    private val environment = createEnvironment()

    private val sut = RedundantWith(Config.empty)

    @Test
    fun `a with call that does not use the receiver is reported`() {
        val code = """
            fun test() {
                with("") {
                    println("1")
                    println("2")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with the lambda in parentheses is reported`() {
        val code = """
            fun test(s: String) {
                with(s, {
                    println("1")
                    println("2")
                })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with an empty lambda is reported`() {
        val code = """
            fun foo(): Int = 10

            fun test() {
                with(foo()) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with a single expression in a return is reported`() {
        val code = """
            fun test(): Int {
                return with(1) {
                    2
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call with several statements as a function body is reported`() {
        val code = """
            fun test() = with("") {
                println()
                1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an outer with call whose receiver is used only by an inner with call is reported`() {
        val code = """
            fun test() {
                with("") {
                    with("a") {
                        this
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a with call that uses an implicit receiver passes`() {
        val code = """
            fun test() {
                with("") {
                    length
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses an explicit this passes`() {
        val code = """
            fun test() {
                with("") {
                    this@with.length
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that calls a member function of the receiver passes`() {
        val code = """
            class MyClass {
                fun f(): String = ""
            }

            fun test() {
                val c = MyClass()
                with(c) {
                    println(f())
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses a member extension in a destructuring declaration passes`() {
        val code = """
            class A

            object B {
                operator fun A.component1(): String = "1"
                operator fun A.component2(): String = "2"
            }

            fun dd() {
                with(B) {
                    val (a, b) = A()
                    println(a + b)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses a member extension iterator passes`() {
        val code = """
            class A

            object B {
                operator fun A.iterator(): Iterator<A> = TODO()
            }

            fun main() {
                with(B) {
                    for (a in A()) {
                        println(a)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that uses a member extension delegate passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class A

            object B {
                operator fun A.getValue(nothing: Nothing?, property: KProperty<*>): Any = TODO()
            }

            fun main() {
                with(B) {
                    val x by A()
                    println(x)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call that provides a context parameter passes`() {
        val code = """
            object Context

            context(s: Context)
            fun bar() {}

            fun foo() {
                with(Context) {
                    bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call with a labeled return passes`() {
        val code = """
            fun test() {
                with("") {
                    return@with
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a with call with several statements used as a value passes`() {
        val code = """
            fun test() {
                val i = with("") {
                    println()
                    1
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
