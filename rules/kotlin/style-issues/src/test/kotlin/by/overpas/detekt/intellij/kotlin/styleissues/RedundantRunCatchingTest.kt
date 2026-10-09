package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantRunCatchingTest {

    private val environment = createEnvironment()

    private val sut = RedundantRunCatching(Config.empty)

    @Test
    fun `runCatching followed by getOrThrow is reported`() {
        val code = """
            fun foo() = runCatching { 42 }.getOrThrow()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runCatching followed by getOrThrow inside a call chain is reported`() {
        val code = """
            fun foo() = runCatching { "" }.getOrThrow().length
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified runCatching followed by getOrThrow is reported`() {
        val code = """
            fun foo() = kotlin.runCatching { 5 }.getOrThrow()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runCatching with a receiver followed by getOrThrow is reported`() {
        val code = """
            fun foo(s: String) = s.runCatching { length }.getOrThrow()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `runCatching followed by getOrNull passes`() {
        val code = """
            fun foo() = runCatching { 42 }.getOrNull()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `runCatching whose lambda returns from the function passes`() {
        val code = """
            fun foo(a: Int): Int {
                return runCatching {
                    if (a % 2 == 0) return 0
                    5
                }.getOrThrow()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a shadowed getOrThrow passes`() {
        val code = """
            fun <T> Result<T>.getOrThrow(): Int = 5

            fun foo() = runCatching { 42 }.getOrThrow()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a shadowed runCatching passes`() {
        val code = """
            fun runCatching(f: () -> Int): Result<Int> = Result.success(f())

            fun foo() = runCatching { 42 }.getOrThrow()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
