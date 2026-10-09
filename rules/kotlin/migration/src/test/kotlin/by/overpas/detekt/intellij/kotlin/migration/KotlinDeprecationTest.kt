package by.overpas.detekt.intellij.kotlin.migration

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinDeprecationTest {

    private val environment = createEnvironment()

    private val sut = KotlinDeprecation(Config.empty)

    @Test
    fun `a call of a deprecated function with a replacement is reported`() {
        val code = """
            @Deprecated("Use newFoo", ReplaceWith("newFoo()"))
            fun oldFoo() = 1

            fun newFoo() = 1

            fun test() = oldFoo()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a deprecated class member with a replacement is reported`() {
        val code = """
            class Foo {
                @Deprecated("Use size", ReplaceWith("size"))
                val length = 1

                val size = 1
            }

            fun test(foo: Foo) = foo.length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an import and a call of a deprecated function with a replacement are reported`() {
        val code = """
            import lib.oldFoo

            fun test() = oldFoo()
        """.trimIndent()
        val dependency = """
            package lib

            @Deprecated("Use newFoo", ReplaceWith("newFoo()"))
            fun oldFoo() = 1

            fun newFoo() = 1

            @Deprecated("No replacement")
            fun legacyFoo() = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a useless cast is reported`() {
        val code = """
            fun test(text: String) = text as String
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unnecessary safe call is reported`() {
        val code = """
            fun test(text: String) = text?.length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unnecessary not-null assertion is reported`() {
        val code = """
            fun test(text: String) = text!!.length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a useless elvis is reported`() {
        val code = """
            fun test(text: String) = text ?: ""
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a misplaced type parameter constraint is reported`() {
        val code = """
            fun <T : Comparable<T>> test(value: T) where T : CharSequence = value.length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a call of a deprecated function without a replacement passes`() {
        val code = """
            @Deprecated("No replacement")
            fun legacyFoo() = 1

            fun test() = legacyFoo()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an import of a deprecated function without a replacement passes`() {
        val code = """
            import lib.legacyFoo

            fun test() = 1
        """.trimIndent()
        val dependency = """
            package lib

            @Deprecated("Use newFoo", ReplaceWith("newFoo()"))
            fun oldFoo() = 1

            fun newFoo() = 1

            @Deprecated("No replacement")
            fun legacyFoo() = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an import of a deprecated function in a file that suppresses deprecation passes`() {
        val code = """
            @file:Suppress("DEPRECATION")

            import lib.oldFoo

            fun test() = oldFoo()
        """.trimIndent()
        val dependency = """
            package lib

            @Deprecated("Use newFoo", ReplaceWith("newFoo()"))
            fun oldFoo() = 1

            fun newFoo() = 1

            @Deprecated("No replacement")
            fun legacyFoo() = 1
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, dependency)

        assertEquals(0, findings.size)
    }

    @Test
    fun `code without deprecated or redundant constructs passes`() {
        val code = """
            fun test(text: String?) = text?.length ?: 0
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
