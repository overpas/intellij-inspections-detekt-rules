package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceReadLineWithReadlnTest {

    private val environment = createEnvironment()

    private val sut = ReplaceReadLineWithReadln(Config.empty)

    @Test
    fun `a readLine call is reported`() {
        val code = """
            fun main() {
                readLine()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a readLine call with a not-null assertion is reported`() {
        val code = """
            fun main() {
                readLine()!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified readLine call with a not-null assertion is reported`() {
        val code = """
            fun main() {
                kotlin.io.readLine()!!
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified readLine call with a safe call is reported`() {
        val code = """
            fun main() {
                println(kotlin.io.readLine()?.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a readLine call with a selector after a not-null assertion is reported`() {
        val code = """
            fun main() {
                println(readLine()!!.length)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a readLine call next to a local readln function is reported`() {
        val code = """
            fun main() {
                readLine()!!
            }

            fun readln() {}
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an imported readLine call with an alias is reported`() {
        val code = """
            import kotlin.io.readLine as input

            fun main() {
                input()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a readln call passes`() {
        val code = """
            fun main() {
                println(readln())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a readlnOrNull call passes`() {
        val code = """
            fun main() {
                println(readlnOrNull())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a local readLine function passes`() {
        val code = """
            fun readLine(): String = ""

            fun main() {
                readLine()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a readLine member function passes`() {
        val code = """
            class Reader {
                fun readLine(): String? = null
            }

            fun main() {
                Reader().readLine()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
