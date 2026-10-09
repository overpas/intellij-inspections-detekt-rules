package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousJavaClassCallableReferenceTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousJavaClassCallableReference(Config.empty)

    @Test
    fun `a javaClass reference on an explicit expression receiver is reported`() {
        val code = """
            fun usage(a: Any) {
                a::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a javaClass reference on an implicit receiver is reported`() {
        val code = """
            fun Any.usage() {
                ::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a javaClass reference on a type receiver is reported`() {
        val code = """
            fun usage() {
                Any::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a javaClass reference on a generic type receiver is reported`() {
        val code = """
            class Generic<T>

            fun usage() {
                Generic<Any>::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a javaClass reference on a nested generic type receiver is reported`() {
        val code = """
            class Generic<T> {
                inner class InnerSimple {
                    inner class InnerGeneric<T>
                }
            }

            fun usage() {
                Generic<Any>.InnerSimple.InnerGeneric<String>::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a reference to an unrelated javaClass property passes`() {
        val code = """
            class MyClass(val javaClass: String)

            fun usage(a: MyClass) {
                a::javaClass
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a javaClass property call passes`() {
        val code = """
            fun usage(a: Any) = a.javaClass
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a class java reference passes`() {
        val code = """
            fun usage() = Any::class.java
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
