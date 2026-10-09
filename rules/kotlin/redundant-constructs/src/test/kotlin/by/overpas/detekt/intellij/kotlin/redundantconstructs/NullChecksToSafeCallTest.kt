package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class NullChecksToSafeCallTest {

    private val environment = createEnvironment()

    private val sut = NullChecksToSafeCall(Config.empty)

    @Test
    fun `not-null checks of a receiver and a property on it are reported`() {
        val code = """
            fun test() {
                val a: Testtt? = Testtt()
                if (a != null && a.a != null && a.a.a != null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `null checks of a receiver and a property on it are reported`() {
        val code = """
            fun test() {
                val a: Testtt? = Testtt()
                if (a == null || a.a == null || a.a.a == null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `not-null checks of a parameter and a function call on it are reported`() {
        val code = """
            class My {
                fun foo(): String? = null
            }

            fun test(my: My?) {
                if (my != null && my.foo() != null) {
                    println()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `not-null checks that start with a safe call are reported`() {
        val code = """
            fun test() {
                val a: Testtt? = Testtt()
                if (a?.a != null && a.a.a != null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `null checks of different receivers pass`() {
        val code = """
            class KotlinType

            class KotlinTypeInfo(val type: KotlinType?) {
                fun foo(other: KotlinTypeInfo) {
                    if (type != null && other.type != null) {
                        println()
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a not-null check of a longer safe call chain passes`() {
        val code = """
            fun test() {
                val a: Testtt? = Testtt()
                if (a != null && a.a?.a != null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `not-null checks of an unstable receiver pass`() {
        val code = """
            var a: Testtt? = Testtt()

            fun test() {
                if (a != null && a?.a != null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `not-null checks with an extension on a nullable receiver pass`() {
        val code = """
            val a: Testtt? = Testtt()

            fun test() {
                if (a != null && a.a != null) {
                    println()
                }
            }

            class Testtt

            val Testtt?.a: Testtt? get() = null
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `mixed null checks pass`() {
        val code = """
            fun test() {
                val a: Testtt? = Testtt()
                if (a != null || a?.a != null) {
                    println()
                }
            }

            class Testtt {
                val a: Testtt? = null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
