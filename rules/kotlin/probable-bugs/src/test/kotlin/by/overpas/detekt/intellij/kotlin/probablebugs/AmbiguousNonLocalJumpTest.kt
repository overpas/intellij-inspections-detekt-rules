package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class AmbiguousNonLocalJumpTest {

    private val environment = createEnvironment()

    private val sut = AmbiguousNonLocalJump(Config.empty)

    @Test
    fun `an unlabeled continue in forEach inside a for loop is reported`() {
        val code = """
            fun foo() {
                for (i in 1..5) {
                    (1..5).forEach {
                        if (it == 2) continue
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled continue in a parenthesized lambda argument is reported`() {
        val code = """
            fun foo() {
                for (i in 1..5) {
                    (1..5).forEach({
                        if (it == 2) continue
                    })
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled break in forEach inside a while loop is reported`() {
        val code = """
            fun foo() {
                while (true) {
                    (1..5).forEach {
                        if (it == 2) break
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled break in forEach inside a do-while loop is reported`() {
        val code = """
            fun foo() {
                do {
                    (1..5).forEach {
                        if (it == 2) break
                    }
                } while (true)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled break in a source inline function without a contract is reported`() {
        val code = """
            fun foo() {
                while (true) {
                    true.ifTrue {
                        break
                    }
                }
            }

            inline fun Boolean.ifTrue(block: () -> Unit) {
                if (this) block()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an unlabeled continue in an anonymous function argument is reported`() {
        val code = """
            fun foo() {
                for (i in 1..5) {
                    (1..5).forEach(fun(it: Int) {
                        if (it == 2) continue
                    })
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a labeled continue passes`() {
        val code = """
            fun foo() {
                loop@ for (i in 1..5) {
                    (1..5).forEach {
                        if (it == 2) continue@loop
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a break in an inline function with an at most once contract passes`() {
        val code = """
            import kotlin.contracts.ExperimentalContracts
            import kotlin.contracts.InvocationKind
            import kotlin.contracts.contract

            fun foo() {
                while (true) {
                    true.ifTrue {
                        break
                    }
                }
            }

            @OptIn(ExperimentalContracts::class)
            inline fun Boolean.ifTrue(block: () -> Unit) {
                contract {
                    callsInPlace(block, InvocationKind.AT_MOST_ONCE)
                }
                if (this) block()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a continue in run with an exactly once contract passes`() {
        val code = """
            fun foo() {
                for (i in 1..5) {
                    run {
                        if (i == 2) continue
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a continue in a loop inside forEach passes`() {
        val code = """
            fun foo() {
                (1..5).forEach {
                    for (i in 1..5) {
                        if (it == 2) continue
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a plain break in a loop passes`() {
        val code = """
            fun foo() {
                for (i in 1..5) {
                    if (i == 2) break
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
