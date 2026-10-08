package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class WhenWithOnlyElseTest {

    private val sut = WhenWithOnlyElse(Config.empty)

    @Test
    fun `a when with only an else branch is reported`() {
        val code = """
            fun foo() {
                val a = when ("") {
                    else -> 1
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when statement with only an else block is reported`() {
        val code = """
            fun foo() {
                when ("") {
                    else -> {
                        println("")
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with an empty else block is reported`() {
        val code = """
            fun foo() {
                when ("") {
                    else -> { }
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with a subject variable and only an else branch is reported`() {
        val code = """
            fun foo() {
                when (val a = create()) {
                    else -> use(a)
                }
            }

            fun create(): String = ""

            fun use(s: String) {}
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subjectless when with only an else branch is reported`() {
        val code = """
            fun foo() {
                when {
                    else -> println("")
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a when with other branches passes`() {
        val code = """
            fun foo() {
                when ("") {
                    "a" -> println("a")
                    else -> println("else")
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
