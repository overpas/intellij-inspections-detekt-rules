package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousCascadingIfTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousCascadingIf(Config.empty)

    @Test
    fun `a binary expression after a cascade of block branches is reported`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) {
                    2
                } else {
                    3
                } + 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade inside an else block is reported`() {
        val code = """
            fun test() {
                if (true) {
                } else {
                    if (true) {
                        1
                    } else if (true) {
                        2
                    } else {
                        3
                    } + 4
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an elvis expression after a cascade is reported`() {
        val code = """
            fun test() {
                if (true) {
                    null
                } else if (true) {
                    Any()
                } else {
                    null
                } ?: Any()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade with many branches is reported once`() {
        val code = """
            fun translateNumber(n: Int, a: Int): String {
                return if (a == 1) {
                    "one"
                } else if (n == 2) {
                    "two"
                } else if (n == 3) {
                    "three"
                } else if (n == 4) {
                    "four"
                } else {
                    "???"
                } + 1
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a qualified call after a cascade is reported`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) {
                    2
                } else {
                    3
                }.let { print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a safe call after a cascade is reported`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) {
                    2
                } else {
                    3
                }?.let { print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an if without else passes`() {
        val code = """
            fun test() {
                if (true) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a cascade without a trailing operator passes`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) {
                    2
                } else {
                    3
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified call after a single else block passes`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else {
                    2
                }.let { print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a binary expression in a last else branch without braces passes`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) 2 else 3 + 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a one line cascade passes`() {
        val code = """
            fun test() {
                if (true) 1 else if (true) 2 else 3 + 4
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified call in a last else branch without braces passes`() {
        val code = """
            fun test() {
                if (true) {
                    1
                } else if (true) 2 else 3.let { print(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a qualified call in a single else branch passes`() {
        val code = """
            fun test() {
                if (true) {
                } else 42.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
