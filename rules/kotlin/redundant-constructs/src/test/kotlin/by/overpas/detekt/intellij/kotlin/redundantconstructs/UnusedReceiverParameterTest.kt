package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class UnusedReceiverParameterTest {

    private val environment = createEnvironment()

    private val sut = UnusedReceiverParameter(Config.empty)

    @Test
    fun `an unused receiver of an extension function is reported`() {
        val code = """
            class CompetitorA
            fun CompetitorA.compete() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused object receiver is reported`() {
        val code = """
            object Test

            fun Test.foo() = 42
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused type parameter receiver is reported`() {
        val code = """
            fun <T> T.foo(t: T) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused receiver of an extension property is reported`() {
        val code = """
            val <T> T.bar: Int
                get() = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused receiver of an anonymous function is reported`() {
        val code = """
            class A {
                fun a() {
                    val f = fun A.() {
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a member extension using only the labeled function receiver is reported`() {
        val code = """
            class Test(val str: String) {
                private fun Test.print() {
                    println(str)
                    println(this@print.str)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unused receiver of a function with a reified class literal is reported`() {
        val code = """
            inline fun <reified T> String.testFun(): String {
                return T::class.simpleName.orEmpty()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a callable reference to a top level function is reported`() {
        val code = """
            fun f() {}

            class C

            fun C.getF() = ::f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a receiver used through this passes`() {
        val code = """
            fun Iterable<Int>.test() {
                for (a in this) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used through an implicit member call passes`() {
        val code = """
            class C {
                fun f() {}
            }

            fun C.g() {
                f()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used by a callable reference passes`() {
        val code = """
            class C {
                fun f() {}
            }

            fun C.getF() = ::f
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used as a context argument passes`() {
        val code = """
            context(_: Int)
            fun other() {}

            fun Int.test() {
                other()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used by a destructuring operator passes`() {
        val code = """
            class A

            object B {
                operator fun A.component1(): String = "1"
                operator fun A.component2(): String = "2"
            }

            fun B.main() {
                val (a, b) = A()
                println(a + b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used by an iterator operator passes`() {
        val code = """
            class A

            object B {
                operator fun A.iterator(): Iterator<A> = TODO()
            }

            fun B.main() {
                for (a in A()) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a receiver used by a delegate operator passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class A

            object B {
                operator fun A.getValue(nothing: Nothing?, property: KProperty<*>): Any = TODO()
            }

            fun B.main() {
                val x by A()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reified receiver type used in a class literal passes`() {
        val code = """
            inline fun <reified T> T.testFun(): String = T::class.simpleName.orEmpty()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a member extension using the labeled class receiver passes`() {
        val code = """
            class Test(val str: String) {
                private fun Test.print() {
                    println(this@Test.str)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reified receiver type used as a type argument passes`() {
        val code = """
            import kotlin.reflect.typeOf

            class Test<T>

            inline fun <reified T> Test<T>.typeOfElements() = typeOf<T>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a type parameter shared with the return type passes`() {
        val code = """
            val <T> T.bar: T?
                get() = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a companion object receiver passes`() {
        val code = """
            class Test {
                companion object
            }

            fun Test.Companion.foo() = 42
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an operator extension passes`() {
        val code = """
            class Test(val test: Int)

            operator fun Test.invoke(x: Int, y: Int) = Test(x + y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an infix extension passes`() {
        val code = """
            class Test(val test: Int)

            infix fun Test.build(x: Int) = Test(x * x)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open member extension passes`() {
        val code = """
            open class Test {
                open fun String.foo() {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an anonymous function with an expected type passes`() {
        val code = """
            fun stateKeeper(block: String.() -> Unit) {}

            fun test() {
                stateKeeper(fun String.() {})
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an anonymous function called with an explicit receiver passes`() {
        val code = """
            class A

            fun test(a: A) {
                a.(fun A.() {
                })()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
