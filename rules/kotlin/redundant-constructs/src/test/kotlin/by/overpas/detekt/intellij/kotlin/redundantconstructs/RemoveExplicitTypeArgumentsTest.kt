package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveExplicitTypeArgumentsTest {

    private val environment = createEnvironment()

    private val sut = RemoveExplicitTypeArguments(Config.empty)

    @Test
    fun `type arguments inferred from a value argument are reported`() {
        val code = """
            fun foo() {
                val x = "x"
                bar<String>(x)
            }

            fun <T> bar(t: T): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments inferred from the function return type are reported`() {
        val code = """
            fun bar(): MutableList<String> = mutableListOf<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments inferred from the property type are reported`() {
        val code = """
            val foo: MutableList<String> = mutableListOf<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments inferred from a getter return type are reported`() {
        val code = """
            val x: List<String>
                get() = listOf<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments of arrayOf are reported`() {
        val code = """
            fun bar(): Array<String> = arrayOf<String>()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `several type arguments inferred from literals are reported`() {
        val code = """
            fun foo() {
                val z = bar<String, Int, Int, String>("1", 1, 2, "x")
            }

            fun <T, V, R, K> bar(t: T, v: V, r: R, k: K): Int = 2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments inferred from the outer call are reported`() {
        val code = """
            class ListWrapper<T>(val x: List<T>)

            val list: ListWrapper<Int> = ListWrapper(listOf<Int>())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `type arguments of a call without expected type pass`() {
        val code = """
            fun foo() {
                val x = listOf<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `type arguments that are a supertype of the inferred type pass`() {
        val code = """
            fun foo() {
                val x = bar<Any>("x")
            }

            fun <T> bar(t: T): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `type arguments of an inline function with a reified parameter pass`() {
        val code = """
            inline fun <reified T> foo(x: T) {}

            fun main() {
                foo<Int>(42)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `type arguments of emptyArray pass`() {
        val code = """
            fun main() {
                val a: Array<Int> = emptyArray<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `annotated type arguments pass`() {
        val code = """
            @Target(AnnotationTarget.TYPE)
            annotation class Foo(val value: String)

            fun main() {
                val l = listOf<@Foo("bar") String>("")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `type arguments that select an overload pass`() {
        val code = """
            fun f1(a: Int, b: Int) {}

            fun f1(a: Any?, b: Any?) {}

            fun <T> f2(): T? = null

            fun f3() {
                f1(42, f2<Int>())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `type arguments that differ from the implicit receiver type pass`() {
        val code = """
            fun <T> T.ext(): T = this

            fun String.testMeToo() {
                fun Int.testOther() {
                    val y = ext<String>()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function type argument that a lambda needs passes`() {
        val code = """
            fun foo() {
                bar<(Int) -> Int>({ baz(it) })
            }

            fun baz(x: Int): Int = x

            fun <T> bar(t: T): Int = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
