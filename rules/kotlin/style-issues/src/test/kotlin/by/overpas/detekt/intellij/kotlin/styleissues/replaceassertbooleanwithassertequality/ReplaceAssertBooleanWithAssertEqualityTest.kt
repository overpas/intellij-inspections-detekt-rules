package by.overpas.detekt.intellij.kotlin.styleissues.replaceassertbooleanwithassertequality

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceAssertBooleanWithAssertEqualityTest {

    private val environment = createEnvironment()

    private val sut = ReplaceAssertBooleanWithAssertEquality(Config.empty)

    @Test
    fun `an assertTrue call with an equality check is reported`() {
        val code = """
            import kotlin.test.assertTrue

            fun foo() {
                val a = "a"
                val b = "a"
                assertTrue(a == b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertTrue call with an identity check is reported`() {
        val code = """
            import kotlin.test.assertTrue

            fun foo() {
                val a = "a"
                val b = "a"
                assertTrue(a === b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertFalse call with an equality check is reported`() {
        val code = """
            import kotlin.test.assertFalse

            fun foo() {
                val a = "a"
                val b = "b"
                assertFalse(a == b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertFalse call with an identity check is reported`() {
        val code = """
            import kotlin.test.assertFalse

            fun foo() {
                val a = "a"
                val b = "b"
                assertFalse(a === b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assertTrue call with an equality check and a message is reported`() {
        val code = """
            import kotlin.test.assertTrue

            fun foo() {
                val a = "a"
                val b = "a"
                assertTrue(a == b, "message")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check of a nullable supertype and a subtype is reported`() {
        val code = """
            import kotlin.test.assertTrue

            interface Parent

            interface Child : Parent

            fun test(p: Parent?, c: Child) {
                assertTrue(c === p)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with a number literal is reported`() {
        val code = """
            import kotlin.test.assertTrue

            fun test(items: List<String>) {
                assertTrue(items.size == 1)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an equality check with a null literal passes`() {
        val code = """
            import kotlin.test.assertTrue

            fun test(failure: Throwable?) {
                assertTrue(failure == null, "no failure")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check with a true literal passes`() {
        val code = """
            import kotlin.test.assertTrue

            fun test(args: List<String>?) {
                assertTrue(args?.contains("-y") == true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an identity check with a parenthesized false literal passes`() {
        val code = """
            import kotlin.test.assertFalse

            fun test(paused: Boolean?) {
                assertFalse((false) === paused)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertTrue call with a boolean variable passes`() {
        val code = """
            import kotlin.test.assertTrue

            fun foo() {
                val isA = true
                assertTrue(isA)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertFalse call with a boolean variable passes`() {
        val code = """
            import kotlin.test.assertFalse

            fun foo() {
                val isA = false
                assertFalse(isA)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an equality check of a supertype and a nullable subtype passes`() {
        val code = """
            import kotlin.test.assertTrue

            interface Parent

            interface Child : Parent

            fun test(p: Parent, c: Child?) {
                assertTrue(p == c)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assertTrue function outside kotlin test passes`() {
        val code = """
            fun assertTrue(condition: Boolean) = check(condition)

            fun foo() {
                val a = "a"
                val b = "a"
                assertTrue(a == b)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
