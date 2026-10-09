package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantNullableReturnTypeTest {

    private val environment = createEnvironment()

    private val sut = RedundantNullableReturnType(Config.empty)

    @Test
    fun `a function with a block body that returns a non-null value is reported`() {
        val code = """
            fun foo(xs: List<Int>): Int? {
                return xs.first()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with an expression body that returns a non-null value is reported`() {
        val code = """
            fun foo(xs: List<Int>): Int? = xs.first()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function that returns a smart cast value is reported`() {
        val code = """
            fun foo(i: Int?): Int? {
                if (i != null) {
                    return i
                } else {
                    return 0
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with a lambda that returns null to the lambda is reported`() {
        val code = """
            fun test(list: List<Int>): Int? {
                val x = list.mapNotNull {
                    return@mapNotNull null
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function with a local function that returns null is reported`() {
        val code = """
            fun test(): Int? {
                fun f(): Int? {
                    return null
                }
                return 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with a non-null initializer is reported`() {
        val code = """
            val foo: String? = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a property with a getter that returns a non-null value is reported`() {
        val code = """
            val foo: Int?
                get() {
                    return 1
                }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function that can return null passes`() {
        val code = """
            fun foo(xs: List<Int>, b: Boolean): Int? = if (b) xs.first() else xs.lastOrNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function without returns passes`() {
        val code = """
            fun foo(): Int? {
                TODO()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an open function passes`() {
        val code = """
            abstract class Foo {
                open fun foo(): String? = ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function that returns null from an inline lambda passes`() {
        val code = """
            class MyClass

            inline fun <T> acceptMyClass(m: (MyClass?) -> T) {}

            fun one(): MyClass? {
                acceptMyClass { return it }
                return MyClass()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a function that returns null in an elvis passes`() {
        val code = """
            fun elvisFun(str: String?): String? {
                val v = str?.length ?: return null
                return v.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property with a getter that can return null passes`() {
        val code = """
            class A(_prop: String) {
                val prop: String? = _prop
                    get() = if (false) field else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a transient property passes`() {
        val code = """
            class FooClass {
                @Transient
                val test: Int? = 5
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a var property passes`() {
        val code = """
            var foo: String? = ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
