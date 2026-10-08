package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyWhenWithBooleanConstantConditionTest {

    private val sut = SimplifyWhenWithBooleanConstantCondition(Config.empty)

    @Test
    fun `a false branch is reported`() {
        val code = """
            fun test() {
                val x = when {
                    false -> 1
                    else -> 2
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a true branch is reported`() {
        val code = """
            fun test() {
                when {
                    true -> println(1)
                    else -> println(2)
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a true branch after other branches is reported`() {
        val code = """
            fun test(i: Int) {
                val x = when {
                    i == 1 -> 1
                    false -> 2
                    true -> 3
                    else -> 4
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with only a false branch is reported`() {
        val code = """
            fun test() {
                when {
                    false -> println(1)
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with a subject passes`() {
        val code = """
            fun test(b: Boolean) {
                when (b) {
                    true -> println(1)
                    false -> println(2)
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a when without constant conditions passes`() {
        val code = """
            fun test(i: Int) {
                when {
                    i == 1 -> println(1)
                    else -> println(2)
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
