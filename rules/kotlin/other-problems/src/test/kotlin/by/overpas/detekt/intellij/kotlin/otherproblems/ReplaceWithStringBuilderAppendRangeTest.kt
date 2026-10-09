package by.overpas.detekt.intellij.kotlin.otherproblems

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceWithStringBuilderAppendRangeTest {

    private val environment = createEnvironment()

    private val sut = ReplaceWithStringBuilderAppendRange(Config.empty)

    @Test
    fun `an append call with a char array and variables is reported`() {
        val code = """
            fun test(charArray: CharArray, offset: Int, len: Int): String {
                return buildString {
                    append(charArray, offset, len)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with an explicit receiver is reported`() {
        val code = """
            fun test(charArray: CharArray, offset: Int, len: Int): String {
                return buildString {
                    this.append(charArray, offset, len)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with constant arguments is reported`() {
        val code = """
            fun test(charArray: CharArray): String {
                return buildString {
                    append(charArray, 2, 4)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with a zero offset is reported`() {
        val code = """
            fun test(charArray: CharArray, len: Int): String {
                return buildString {
                    append(charArray, 0, len)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with a nullable char array is reported`() {
        val code = """
            fun test(charArray: CharArray?, len: Int): String {
                return buildString {
                    append(charArray, 0, len)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with a null-checked char array is reported`() {
        val code = """
            fun test(charArray: CharArray?, len: Int): String {
                return buildString {
                    if (charArray != null) {
                        append(charArray, 0, len)
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with binary expressions is reported`() {
        val code = """
            fun test(charArray: CharArray, a: Int, b: Int, c: Int, d: Int): String {
                return buildString {
                    append(charArray, a - b, c - d)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call on a string builder variable is reported`() {
        val code = """
            fun test(charArray: CharArray): String {
                val builder = StringBuilder()
                builder.append(charArray, 1, 2)
                return builder.toString()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an append call with a char sequence range passes`() {
        val code = """
            fun test(text: CharSequence): String {
                return buildString {
                    append(text, 1, 2)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an append call with a char array only passes`() {
        val code = """
            fun test(charArray: CharArray): String {
                return buildString {
                    append(charArray)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an appendRange call passes`() {
        val code = """
            fun test(charArray: CharArray): String {
                return buildString {
                    appendRange(charArray, 1, 3)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
