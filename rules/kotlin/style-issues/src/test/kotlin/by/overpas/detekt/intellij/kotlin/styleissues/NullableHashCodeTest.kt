package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class NullableHashCodeTest {

    private val environment = createEnvironment()

    private val sut = NullableHashCode(Config.empty)

    @Test
    fun `a safe hashCode call with a zero default is reported`() {
        val code = """
            fun hash(value: String?): Int {
                return value?.hashCode() ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe call of an overridden hashCode is reported`() {
        val code = """
            class WithHashCode(private val value: Int) {
                override fun hashCode(): Int = value * 31
            }

            fun hash(value: WithHashCode?): Int {
                return value?.hashCode() ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call with a comment before the elvis is reported`() {
        val code = """
            fun hash(value: String?): Int {
                return value?.hashCode() /* keep me */ ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized safe hashCode call is reported`() {
        val code = """
            fun hash(value: String?): Int {
                return (value?.hashCode()) ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call inside an arithmetic expression is reported`() {
        val code = """
            fun test(value: String?): Int = (1 + (value?.hashCode() ?: 0)) * 31
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call on a parenthesized receiver is reported`() {
        val code = """
            fun hash(value: String?): Int {
                return (value)?.hashCode() ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a parenthesized elvis expression is reported`() {
        val code = """
            fun hash(value: String?): Int {
                return (value?.hashCode() ?: 0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call on a value of a generic type is reported`() {
        val code = """
            fun <T> hash(value: T): Int = value?.hashCode() ?: 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe hashCode call with a non-zero default passes`() {
        val code = """
            fun hash(value: String?): Int {
                return value?.hashCode() ?: 31
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe hashCode call on a non-null receiver passes`() {
        val code = """
            fun hash(value: String): Int {
                return value?.hashCode() ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe call of another function with a zero default passes`() {
        val code = """
            fun length(value: String?): Int {
                return value?.length ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe call of an unrelated hashCode function passes`() {
        val code = """
            class Hasher {
                fun hashCode(seed: Int): Int = seed
            }

            fun hash(value: Hasher?): Int {
                return value?.hashCode(1) ?: 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a hashCode extension on a nullable receiver passes`() {
        val code = """
            fun hash(value: String?): Int {
                return value.hashCode()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
