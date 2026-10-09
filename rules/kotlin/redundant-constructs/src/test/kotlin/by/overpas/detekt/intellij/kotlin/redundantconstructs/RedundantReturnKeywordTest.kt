package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantReturnKeywordTest {

    private val environment = createEnvironment()

    private val sut = RedundantReturnKeyword(Config.empty)

    @Test
    fun `a return in a branch of a returned if-else chain is reported`() {
        val code = """
            fun foo(x: Int): String {
                return if (x < 0) {
                    "negative"
                } else if (x == 42) {
                    return "6 * 7"
                } else {
                    "some"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `returns in several branches without braces are reported`() {
        val code = """
            fun foo(x: Int): String {
                return if (x < 0) {
                    "negative"
                } else if (x == 42)
                    return "6 * 7"
                else {
                    return "some"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `returns in branches of a returned when are reported`() {
        val code = """
            fun foo(x: Int): String {
                return when {
                    x < 0 -> {
                        print("negative")
                        return "negative"
                    }
                    x == 0 -> "zero"
                    else -> return "many"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a return in the right side of a returned elvis is reported`() {
        val code = """
            fun test(value: String?): String {
                return value ?: return "default"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return deep in nested parenthesized branches is reported`() {
        val code = """
            fun foo(input: String): String {
                return ((when {
                    input.startsWith("foo") ->
                        when {
                            input.contains("bar") -> {
                                println("x")
                                ((if (input.endsWith("baz")) {
                                    if (input.isEmpty()) {
                                        "a"
                                    } else if (input.startsWith("c"))
                                        input.takeIf { it.length > 1 } ?: return "d"
                                    else {
                                        "e"
                                    }
                                } else {
                                    "f"
                                }))
                            }
                            else -> "g"
                        }
                    else -> "h"
                }))
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return of Unit in a returned if is reported`() {
        val code = """
            fun foo(flag: Boolean) {
                return if (flag) {
                    println("yes")
                } else {
                    println("no")
                    return
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return of a lambda in a returned if is reported`() {
        val code = """
            fun foo(flag: Boolean): (Int) -> Boolean {
                return if (flag) {
                    print(42)
                    return { _: Int -> true }
                } else {
                    { _: Int -> false }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a return of a labeled lambda passes`() {
        val code = """
            fun foo(flag: Boolean): (Int) -> Boolean {
                return if (flag) {
                    print(42)
                    return bar@{ _: Int ->
                        return@bar true
                    }
                } else {
                    bar@{ _: Int ->
                        return@bar true
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return that is not the last statement of its branch passes`() {
        val code = """
            fun foo(x: Int): String {
                return if (x > 0) {
                    if (x == 1) {
                        return "one"
                    }
                    "many"
                } else {
                    "none"
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return in an if that is not returned passes`() {
        val code = """
            fun foo(x: Int): String {
                if (x < 0) {
                    "negative"
                } else if (x == 42) {
                    return "6 * 7"
                } else {
                    "some"
                }
                return ""
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return in a when assigned to a variable passes`() {
        val code = """
            fun foo(x: Int): String {
                val s = when {
                    x < 0 -> return "negative"
                    else -> "many"
                }
                return s
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a return of null in a returned elvis passes`() {
        val code = """
            fun bar(): Int = 42

            fun test(): Int? {
                return bar() ?: return null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an unreachable return passes`() {
        val code = """
            fun foo(num: Int): String {
                return if (num == 0) {
                    return throw IllegalArgumentException()
                } else "non-zero"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
