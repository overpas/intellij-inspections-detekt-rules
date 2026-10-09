package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousCallableReferenceInLambdaTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousCallableReferenceInLambda(Config.empty)

    @Test
    fun `a bound reference to the lambda parameter is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).map { it::toString }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unbound reference is reported`() {
        val code = """
            fun foo() {
                val x = listOf(1, 2, 3).map { Int::toString }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference with a named lambda parameter is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).map { bar -> bar::toString }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to a captured value is reported`() {
        val code = """
            class C {
                fun method(): String = ""
            }

            fun caller(c: C, l: List<C>) {
                l.map { c::method }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference in a trailing lambda after other arguments is reported`() {
        val code = """
            fun foo() {
                x(1) { ::y }
            }

            fun y() {}

            fun x(number: Int, func: () -> Unit) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference in a lambda that expects a function type passes`() {
        val code = """
            fun foo(arg: Int) = arg.toString()

            fun bar(f: () -> (Int) -> String) {}

            val someFun = bar { ::foo }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference in a lambda that expects a KProperty passes`() {
        val code = """
            import kotlin.reflect.KProperty

            class C(val foo: String)

            fun bar(f: () -> KProperty<*>) {}

            fun test(c: C) {
                bar { c::foo }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference in a lambda that expects a generic function type passes`() {
        val code = """
            class C {
                fun foo() {}
            }

            fun <T : Function<*>> bar(f: () -> T) {}

            fun test(c: C) {
                bar { c::foo }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference in a lambda whose result has an explicit type passes`() {
        val code = """
            import kotlin.reflect.KFunction0

            val foo: List<KFunction0<String>> = listOf(1, 2, 3).map { it::toString }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference in a lambda whose result is passed as an argument passes`() {
        val code = """
            import kotlin.reflect.KFunction0

            fun test() {
                foo(listOf(1, 2, 3).map { it::toString })
            }

            fun foo(list: List<KFunction0<String>>) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a reference in a lambda whose result is returned passes`() {
        val code = """
            import kotlin.reflect.KFunction0

            fun foo(): List<KFunction0<String>> {
                return listOf(1, 2, 3).map { it::toString }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda with several statements passes`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).map {
                    println(it)
                    Int::toString
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a lambda without a callable reference passes`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).map { println(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
