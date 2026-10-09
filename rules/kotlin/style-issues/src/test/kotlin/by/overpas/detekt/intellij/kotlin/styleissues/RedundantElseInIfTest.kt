package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantElseInIfTest {

    private val environment = createEnvironment()

    private val sut = RedundantElseInIf(Config.empty)

    @Test
    fun `an else after a throwing then branch is reported`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun test(x: Boolean) {
                if (x) throw SomeException()
                else foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else after a block ending with return is reported`() {
        val code = """
            fun foo(): Int = 1

            fun bar(): Int = 2

            fun test(x: Boolean) {
                if (x) {
                    return
                } else {
                    foo()
                    bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else after else-if branches that all jump is reported`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun test(x: Boolean, y: Boolean) {
                if (x) throw SomeException()
                else if (y) return
                else foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else on the same line as a throwing branch is reported`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun test(x: Boolean): Int {
                if (x) throw SomeException() else return foo()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else inside a lambda after a returning branch is reported`() {
        val code = """
            fun foo() {}

            fun bar() {}

            fun test(s: String?, b: Boolean) {
                s?.also {
                    if (b) {
                        foo()
                        return
                    } else {
                        bar()
                        return
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an else after an empty else-if branch passes`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun test(x: Boolean, y: Boolean) {
                if (x) {
                    throw SomeException()
                } else if (y) {
                } else {
                    foo()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an else-if chain without a final else passes`() {
        val code = """
            class SomeException : RuntimeException()

            fun test(x: Boolean, y: Boolean) {
                if (x) {
                    throw SomeException()
                } else if (y) {
                    return
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an else after a branch that does not jump passes`() {
        val code = """
            fun foo(): Int = 1

            fun bar(): Int = 2

            fun test(x: Boolean, y: Boolean) {
                if (x) foo()
                else if (y) return
                else bar()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an else after a block that does not end with a jump passes`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun bar(): Int = 2

            fun test(x: Boolean, y: Boolean) {
                if (x) {
                    foo()
                } else if (y) {
                    throw SomeException()
                } else {
                    foo()
                    bar()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an else of an if used as an expression passes`() {
        val code = """
            class SomeException : RuntimeException()

            fun foo(): Int = 1

            fun test(x: Boolean, y: Boolean) {
                val i: Int = if (x) throw SomeException()
                else if (y) return
                else foo()
                println(i)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
