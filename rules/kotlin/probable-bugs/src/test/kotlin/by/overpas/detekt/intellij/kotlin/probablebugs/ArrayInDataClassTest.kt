package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ArrayInDataClassTest {

    private val environment = createEnvironment()

    private val sut = ArrayInDataClass(Config.empty)

    @Test
    fun `a generic array property in a data class is reported`() {
        val code = """
            data class A(val a: Array<String>)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a primitive array property in a data class is reported`() {
        val code = """
            data class A(val a: IntArray)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `every array property of a data class is reported`() {
        val code = """
            class MyClass

            data class A(
                val a: IntArray,
                val b: Array<String>,
                val c: String,
                val d: Int,
                val e: MyClass,
            )
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a data class with only equals is reported`() {
        val code = """
            data class A(val a: IntArray) {
                override fun equals(other: Any?): Boolean = other is A && a.contentEquals(other.a)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a data class with only hashCode is reported`() {
        val code = """
            data class A(val a: IntArray) {
                override fun hashCode(): Int = a.contentHashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a data class with an equals overload is reported`() {
        val code = """
            data class A(val a: IntArray) {
                fun equals(other: Any?, excessive: Any?): Boolean = true

                override fun hashCode(): Int = a.contentHashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a data class whose parent overrides equals is reported`() {
        val code = """
            open class Parent {
                override fun equals(other: Any?): Boolean = super.equals(other)

                override fun hashCode(): Int = super.hashCode()
            }

            data class A(val a: IntArray) : Parent()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an array property in a value class is reported`() {
        val code = """
            @JvmInline
            value class A(val a: IntArray)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a data class with equals and hashCode passes`() {
        val code = """
            data class A(val a: IntArray) {
                override fun equals(other: Any?): Boolean = other is A && a.contentEquals(other.a)

                override fun hashCode(): Int = a.contentHashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a data class without array properties passes`() {
        val code = """
            data class A(val str: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an array property in a regular class passes`() {
        val code = """
            class A(val a: Array<String>)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a value class without array properties passes`() {
        val code = """
            @JvmInline
            value class A(val a: String)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
