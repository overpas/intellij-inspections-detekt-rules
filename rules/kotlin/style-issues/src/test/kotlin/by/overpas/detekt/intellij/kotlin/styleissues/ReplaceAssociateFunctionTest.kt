package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceAssociateFunctionTest {

    private val environment = createEnvironment()

    private val sut = ReplaceAssociateFunction(Config.empty)

    @Test
    fun `an associate call with the element as the value is reported`() {
        val code = """
            fun getKey(i: Int): Long = 1L

            fun test() {
                listOf(1).associate { getKey(it) to it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call with the element as the key is reported`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test() {
                listOf(1).associate { it to getValue(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call on an array is reported`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test() {
                intArrayOf(1).associate { it to getValue(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call on a sequence is reported`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test() {
                sequenceOf(1).associate { it to getValue(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call with a named parameter and a Pair constructor is reported`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test() {
                listOf(1).associate { i -> Pair(i, getValue(i)) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call with a computed key and value is reported`() {
        val code = $$"""
            fun getKey(i: Int): Long = 1L
            fun getValue(i: Int): String = ""

            fun test() {
                listOf(1).associate { "$it" to getValue(it) }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call with a multiline lambda that returns the element as the key is reported`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test() {
                listOf(1).associate {
                    val value = getValue(it)
                    it to value
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associateTo call is reported`() {
        val code = """
            fun getKey(i: Int): Long = 1L

            fun test() {
                val destination = mutableMapOf<Long, Int>()
                listOf(1).associateTo(destination, { getKey(it) to it })
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an associate call with a multiline lambda and a computed key and value passes`() {
        val code = """
            fun getKey(i: Int): Long = 1L
            fun getValue(i: Int): String = ""

            fun test() {
                listOf(1).associate {
                    val key = getKey(it)
                    val value = getValue(it)
                    key to value
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an associate call with a labeled return passes`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test(b: Boolean) {
                listOf(1).associate {
                    if (b) {
                        return@associate it to ""
                    }
                    it to getValue(it)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an associate call whose lambda ends with an if expression passes`() {
        val code = """
            fun getValue(i: Int): String = ""

            fun test(b: Boolean) {
                listOf(1).associate {
                    if (b) {
                        it to getValue(it)
                    } else {
                        it to ""
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an associate function outside the standard library passes`() {
        val code = """
            class Box(val value: Int) {
                fun associate(transform: (Int) -> Pair<Int, Int>) = transform(value)
            }

            fun test() {
                Box(1).associate { it to it }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
