package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrateDiagnosticSuppressionTest {

    private val environment = createEnvironment()

    private val sut = MigrateDiagnosticSuppression(Config.empty)

    @Test
    fun `an old diagnostic name in a suppression is reported`() {
        val code = """
            @Suppress("HEADER_WITHOUT_IMPLEMENTATION")
            class Dummy
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `each old diagnostic name in a suppression is reported`() {
        val code = """
            @Suppress(
                "HEADER_CLASS_CONSTRUCTOR_DELEGATION_CALL",
                "HEADER_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER",
                "HEADER_ENUM_CONSTRUCTOR",
            )
            class Dummy
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `an old diagnostic name next to a current one is reported`() {
        val code = """
            @Suppress("IMPL_MISSING", "UNUSED_PARAMETER")
            fun foo(x: Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an old diagnostic name in a suppression on an expression is reported`() {
        val code = """
            fun foo(): Int {
                @Suppress("IMPLEMENTATION_WITHOUT_HEADER")
                val x = 1
                return x
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a new diagnostic name passes`() {
        val code = """
            @Suppress("NO_ACTUAL_FOR_EXPECT")
            class Dummy
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unknown diagnostic name passes`() {
        val code = """
            @Suppress("UNUSED_PARAMETER")
            fun foo(x: Int) {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an old diagnostic name in another annotation passes`() {
        val code = """
            annotation class Suppressed(vararg val names: String)

            @Suppressed("HEADER_WITHOUT_IMPLEMENTATION")
            class Dummy
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an old diagnostic name in a custom annotation named Suppress passes`() {
        val code = """
            package com.example

            annotation class Suppress(vararg val names: String)

            @Suppress("HEADER_WITHOUT_IMPLEMENTATION")
            class Dummy
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
