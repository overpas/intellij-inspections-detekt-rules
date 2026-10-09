package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceIfExpressionWithFirstOrNullTest {

    private val environment = createEnvironment()

    private val sut = ReplaceIfExpressionWithFirstOrNull(Config.empty)

    @Test
    fun `a size check that is not zero on an array is reported`() {
        val code = """
            fun test(values: IntArray): Int? {
                return if (values.size != 0) {
                    values[0]
                } else {
                    null
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an isEmpty check on an array with null in the then branch is reported`() {
        val code = """
            fun test(values: IntArray): Int? {
                return if (values.isEmpty()) {
                    null
                } else {
                    values[0]
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a size less than one check is reported`() {
        val code = """
            fun test(values: IntArray): Int? {
                return if (values.size < 1) null else values[0]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a count call without arguments is reported`() {
        val code = """
            fun test(list: List<Int>): Int? {
                return if (list.count() > 0) list[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a first call in the value branch is reported`() {
        val code = """
            fun test(values: IntArray): Int? {
                return if (values.isNotEmpty()) values.first() else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an explicit get call with a zero index is reported`() {
        val code = """
            fun test(children: List<String>): String? {
                return if (children.isNotEmpty()) {
                    children.get(0)
                } else {
                    null
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a zero literal on the left of the comparison is reported`() {
        val code = """
            fun test(list: List<String>): String? {
                return if (0 < list.size) list[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a string emptiness check is reported`() {
        val code = """
            fun test(text: String): Char? {
                return if (text.isNotEmpty()) text[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a this receiver is reported`() {
        val code = """
            fun IntArray.test(): Int? {
                return if (this.size > 0) this[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an implicit receiver of a with call is reported`() {
        val code = """
            fun test(values: IntArray): Int? {
                return with(values) {
                    if (isNotEmpty()) values[0] else null
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a function call receiver passes`() {
        val code = """
            fun makeChildren(): List<String> = listOf("Emma", "Liam")

            fun test(): String? {
                return if (makeChildren().isNotEmpty()) makeChildren()[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a compound condition passes`() {
        val code = """
            fun test(list: List<String>, predicate: Boolean): String? {
                return if (list.size > 0 && predicate) list[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a count call with a lambda passes`() {
        val code = """
            fun test(list: List<Int>): Int? {
                return if (list.count { it > 0 } > 0) list[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a different receiver in the value branch passes`() {
        val code = """
            class Branch(val size: Int, val c: List<String>)

            class Root(val b: Branch)

            fun test(a: Root, list1: List<String>, list2: List<String>): String? {
                val first = if (list1.size > 0) list2[0] else null
                return if (a.b.size > 0) a.b.c[0] else first
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a map receiver passes`() {
        val code = """
            fun test(values: Map<Int, String>): String? {
                return if (values.isNotEmpty()) values[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-null else branch passes`() {
        val code = """
            fun test(s: String): Char {
                return if (s.isNotEmpty()) s[0] else 'x'
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a non-zero index passes`() {
        val code = """
            fun test(children: List<String>): String? {
                return if (children.size > 0) children[1] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a size greater than one check passes`() {
        val code = """
            fun test(list: List<String>): String? {
                return if (list.size > 1) list[0] else null
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `swapped branches without an inverted condition pass`() {
        val code = """
            fun test(list: List<String>): String? {
                return if (list.size > 0) null else list[0]
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
