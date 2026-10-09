package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class IfExpressionWithIdenticalBranchesTest {

    private val environment = createEnvironment()

    private val sut = IfExpressionWithIdenticalBranches(Config.empty)

    @Test
    fun `identical braced and unbraced branches are reported`() {
        val code = """
            fun test(flag: Boolean): Int = if (flag) { 42 } else 42
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical statement blocks are reported`() {
        val code = """
            fun test(flag: Boolean) {
                val localFlag = flag
                if (localFlag) {
                    println("same")
                    println("same again")
                } else {
                    println("same")
                    println("same again")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical blocks with different comments are reported`() {
        val code = """
            fun test(flag: Boolean) {
                if (flag) {
                    println("same")
                    // Keep this explanation.
                    println("same again")
                } else {
                    println("same")
                    // Keep this explanation too.
                    println("same again")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical blocks with local declarations are reported`() {
        val code = """
            fun test(flag: Boolean) {
                if (flag) {
                    val value = 42
                    println(value)
                } else {
                    val value = 42
                    println(value)
                }
                val value = "outside"
                println(value)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `empty branches are reported`() {
        val code = """
            fun test(flag: Boolean) {
                if (flag) {} else {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical branches with a side effecting condition are reported`() {
        val code = """
            fun nextFlag(): Boolean = true

            fun test(): Int = if (nextFlag()) 42 else 42
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical throw branches are reported`() {
        val code = """
            fun test(flag: Boolean): Nothing =
                if (flag) throw Exception() else throw Exception()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `identical branches with escaping jumps are reported`() {
        val code = """
            fun test(flag: Boolean) {
                while (true) {
                    if (flag) {
                        println(1)
                        break
                    } else {
                        println(1)
                        break
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `different branches pass`() {
        val code = """
            fun test(flag: Boolean): Int = if (flag) 42 else 43
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if without else passes`() {
        val code = """
            fun test(flag: Boolean) {
                if (flag) println("same")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the same text resolved to different calls passes`() {
        val code = """
            private fun Any.result(): Int = 0
            private fun String.result(): Int = 1

            fun test(value: Any): Int =
                if (value is String) value.result() else value.result()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
