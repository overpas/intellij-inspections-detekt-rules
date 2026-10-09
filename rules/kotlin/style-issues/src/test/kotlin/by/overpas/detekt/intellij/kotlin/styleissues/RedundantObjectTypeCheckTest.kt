package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantObjectTypeCheckTest {

    private val environment = createEnvironment()

    private val sut = RedundantObjectTypeCheck(Config.empty)

    @Test
    fun `an is check against an object is reported`() {
        val code = """
            object O

            fun foo(arg: Any) {
                if (arg is O) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a negated is check against an object is reported`() {
        val code = """
            object O

            fun foo(arg: Any) {
                if (arg !is O) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an is check against a companion object is reported`() {
        val code = """
            class C {
                companion object
            }

            fun foo(arg: Any) = arg is C.Companion
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an is check against a class passes`() {
        val code = """
            class C

            fun foo(arg: Any) {
                if (arg is C) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an is check against a data object passes`() {
        val code = """
            data object O

            fun foo(arg: Any) {
                if (arg is O) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated is check against a data object passes`() {
        val code = """
            data object O

            fun foo(arg: Any) {
                if (arg !is O) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an is condition of a when entry against an object passes`() {
        val code = """
            object O

            fun foo(arg: Any) {
                when (arg) {
                    is O -> {
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a negated is condition of a when entry against an object passes`() {
        val code = """
            object O

            fun foo(arg: Any) {
                when (arg) {
                    !is O -> {
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
