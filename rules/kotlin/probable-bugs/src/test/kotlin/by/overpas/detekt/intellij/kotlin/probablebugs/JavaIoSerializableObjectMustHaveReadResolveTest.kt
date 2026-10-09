package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaIoSerializableObjectMustHaveReadResolveTest {

    private val environment = createEnvironment()

    private val sut = JavaIoSerializableObjectMustHaveReadResolve(Config.empty)

    @Test
    fun `a serializable object without readResolve is reported`() {
        val code = """
            object Foo : java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with a backticked name is reported`() {
        val code = """
            object `6-7` : java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an object that implements Serializable through another interface is reported`() {
        val code = """
            interface Bar : java.io.Serializable

            interface Baz : Bar

            object Foo : Baz
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable data object without readResolve is reported`() {
        val code = """
            data object Foo : java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with a readResolve that has parameters is reported`() {
        val code = """
            object Foo : java.io.Serializable {
                private fun readResolve(param: Int): Any = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with an internal readResolve is reported`() {
        val code = """
            object Foo : java.io.Serializable {
                internal fun readResolve(): Any = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with a readResolve that does not return Any is reported`() {
        val code = """
            object Foo : java.io.Serializable {
                private fun readResolve(): Foo = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with a private readResolve in the superclass is reported`() {
        val code = """
            open class Super {
                private fun readResolve(): Any = Foo
            }

            object Foo : Super(), java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable named companion object without readResolve is reported`() {
        val code = """
            class Owner {
                companion object Factory : java.io.Serializable
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a serializable object with a private readResolve passes`() {
        val code = """
            object Foo : java.io.Serializable {
                private fun readResolve(): Any = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a serializable object with a public readResolve passes`() {
        val code = """
            object Foo : java.io.Serializable {
                fun readResolve(): Any = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a serializable data object with a readResolve passes`() {
        val code = """
            data object Foo : java.io.Serializable {
                fun readResolve(): Any = Foo
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a serializable object with a protected readResolve in the superclass passes`() {
        val code = """
            open class Super {
                protected fun readResolve(): Any = Foo
            }

            object Foo : Super(), java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a serializable object with a public readResolve in the superclass passes`() {
        val code = """
            open class Super {
                fun readResolve(): Any = Foo
            }

            object Foo : Super(), java.io.Serializable
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an object that does not implement Serializable passes`() {
        val code = """
            object Foo
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a serializable object literal passes`() {
        val code = """
            fun foo() {
                val literal = object : java.io.Serializable {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
