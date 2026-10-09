package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinEqualsBetweenInconvertibleTypesTest {

    private val environment = createEnvironment()

    private val sut = KotlinEqualsBetweenInconvertibleTypes(Config.empty)

    @Test
    fun `an equals call between an Int and a Long is reported`() {
        val code = """
            fun test(x: Int, y: Long): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between a String and an Int is reported`() {
        val code = """
            fun test(x: String, y: Int): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between two different enums is reported`() {
        val code = """
            enum class E1

            enum class E2

            fun test(x: E1, y: E2): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between an Int and an enum is reported`() {
        val code = """
            enum class E1

            fun test(x: Int, y: E1): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe equals call between a nullable enum and a String is reported`() {
        val code = """
            enum class E1

            fun test(x: E1?, y: String): Boolean? = x?.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between a String and a nullable Int is reported`() {
        val code = """
            fun test(x: String, y: Int?): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equals call between two values of the same enum passes`() {
        val code = """
            enum class E1

            fun test(x: E1, y: E1): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call between two Ints passes`() {
        val code = """
            fun test(x: Int, y: Int): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call between a String and a nullable String passes`() {
        val code = """
            fun test(x: String, y: String?): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a safe equals call on a platform String passes`() {
        val code = """
            fun test(): Boolean? = System.getProperty("some")?.equals("true")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call between an Int and a user type passes`() {
        val code = """
            class Foo

            fun test(x: Int, y: Foo): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equals call between a user type and an Int passes`() {
        val code = """
            class Foo

            fun test(x: Foo, y: Int): Boolean = x.equals(y)
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
