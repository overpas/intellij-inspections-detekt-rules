package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyAssertNotNullTest {

    private val environment = createEnvironment()

    private val sut = SimplifyAssertNotNull(Config.empty)

    @Test
    fun `an assert of the previous variable is reported`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                assert(v != null)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assert of the previous variable with a message lambda is reported`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                assert(v != null, { "Should be not null" })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assert of the previous variable with a trailing message lambda is reported`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                assert(v != null) { "Should be not null" }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assert with comments is reported`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                // now let's check it for null
                assert(v != null /* null */) // 'v' should not be null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an assert with a message lambda of two statements passes`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                assert(v != null, { val t = 1; "Should be not null: " + t })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assert of an equality to null passes`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v = p[0]
                assert(v == null)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a call of a custom assert function passes`() {
        val code = """
            class C {
                fun assert(b: Boolean) {}

                fun foo(p: Array<String?>) {
                    val v = p[0]
                    assert(v != null)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assert of another variable passes`() {
        val code = """
            fun foo(p: Array<String?>) {
                val v1 = p[0]
                val v2 = p[1]
                assert(v1 != null)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an assert of a qualified property passes`() {
        val code = """
            class C(val v: String?) {
                fun foo(p: Array<String?>) {
                    val v = p[0]
                    assert(this.v != null)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
