package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinThrowableNotThrownTest {

    private val environment = createEnvironment()

    private val sut = KotlinThrowableNotThrown(Config.empty)

    @Test
    fun `an exception constructor call used as a statement is reported`() {
        val code = """
            class FooException : RuntimeException()

            fun test() {
                FooException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a call that returns an exception used as a statement is reported`() {
        val code = """
            class FooException : RuntimeException()

            fun createError() = FooException()

            fun test() {
                createError()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `exceptions in the branches of an if statement are reported`() {
        val code = """
            class FooException : RuntimeException()

            fun createException() = FooException()

            fun test(i: Int) {
                if (i == 1) {
                    FooException()
                } else {
                    createException()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `an exception stored in an unused local variable is reported`() {
        val code = """
            class FooException : Exception()

            fun test() {
                val e = FooException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `exceptions of a when expression stored in an unused local variable are reported`() {
        val code = """
            class FooException : RuntimeException()

            fun createException() = FooException()

            fun test(i: Int) {
                val e = when (i) {
                    1 -> FooException()
                    else -> createException()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a thrown exception passes`() {
        val code = """
            class FooException : RuntimeException()

            fun test() {
                throw FooException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a thrown when expression passes`() {
        val code = """
            class FooException : RuntimeException()

            fun createException() = FooException()

            fun test(i: Int) {
                throw when (i) {
                    1 -> FooException()
                    else -> createException()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exception stored in a local variable that is thrown later passes`() {
        val code = """
            class FooException : RuntimeException()

            fun createException() = FooException()

            fun test(i: Int) {
                val e = if (i == 1) {
                    FooException()
                } else {
                    createException()
                }
                throw e
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exception stored in a local variable that is read later passes`() {
        val code = """
            class FooException : Exception()

            fun test(): Exception {
                val e = FooException()
                val e2 = e
                return e2
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exception stored in a class property passes`() {
        val code = """
            class FooException : Exception()

            class Test {
                val e = FooException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a returned exception passes`() {
        val code = """
            class FooException : RuntimeException()

            class BarException : RuntimeException()

            fun test(i: Int?): RuntimeException {
                val x = i ?: return FooException()
                return BarException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an exception returned from a lambda passes`() {
        val code = """
            import java.util.Optional

            fun test(): Int = Optional.of(42).orElseThrow {
                IllegalStateException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call that returns a nullable exception passes`() {
        val code = """
            fun fooException(): RuntimeException? = RuntimeException()

            fun test() {
                fooException()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call that always throws passes`() {
        val code = """
            fun fooError(): Nothing = throw RuntimeException()

            fun test() {
                fooError()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
