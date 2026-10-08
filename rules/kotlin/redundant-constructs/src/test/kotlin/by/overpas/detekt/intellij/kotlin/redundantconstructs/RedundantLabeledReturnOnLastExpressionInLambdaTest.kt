package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantLabeledReturnOnLastExpressionInLambdaTest {

    private val sut = RedundantLabeledReturnOnLastExpressionInLambda(Config.empty)

    @Test
    fun `a labeled return on the last expression is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).find {
                    return@find true
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return with an explicit lambda label is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).find label@{
                    return@label true
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return without a value is reported`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).forEach {
                    return@forEach
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return in an inner lambda is reported`() {
        val code = """
            fun foo() {
                run {
                    run {
                        return@run
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled return in a nested if passes`() {
        val code = """
            fun bar(result: Result<String?>) {
                result.onSuccess {
                    if (it == null) {
                        return@onSuccess
                    }
                    println(it)
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled return in a loop passes`() {
        val code = """
            fun foo(list: List<Int>) {
                list.forEach {
                    for (i in 1..10) {
                        if (i > 5) {
                            return@forEach
                        }
                    }
                    println("done")
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled return outside of a lambda passes`() {
        val code = """
            fun foo(): Boolean {
                return@foo true
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return without a label passes`() {
        val code = """
            fun foo(): Boolean {
                listOf(1, 2, 3).find {
                    return true
                }
                return false
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a labeled return before the last expression passes`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).find {
                    return@find true
                    false
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return to an outer lambda passes`() {
        val code = """
            fun foo() {
                listOf(1, 2, 3).forEach {
                    listOf(1, 2, 3).find {
                        return@forEach
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
