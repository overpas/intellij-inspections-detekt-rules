package by.overpas.detekt.intellij.kotlin.styleissues

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CascadeIfTest {

    private val environment = createEnvironment()

    private val sut = CascadeIf(Config.empty)

    @Test
    fun `a cascade of equality and range checks on one subject is reported`() {
        val code = """
            fun test(i: Int) {
                if (i == 1) {
                    println("a")
                }
                else if (i == 2) println("b")
                else if (i in listOf(3, 4, 5)) println("c")
                else println("none")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade of type and equality checks without a final else is reported`() {
        val code = """
            fun foo(a: Any) {
                if (a == "") {
                    println(a)
                }
                else if (a is String) {
                    println(a)
                }
                else if (a is List<*>) {
                    println(a.size)
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade nested in another if is reported`() {
        val code = """
            fun test(x: Double, i: Int) {
                if (x > 0.0) {
                    if (i == 1) {
                        println("a")
                    }
                    else if (i == 2) {
                        println("b")
                    }
                    else {
                        println("none")
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade with disjunctions and conjunctions on one subject is reported`() {
        val code = """
            fun test(i: Int, flag: Boolean) {
                if (i == 1 || i == 2) {
                    println("a")
                } else if (i == 3 && flag) {
                    println("b")
                } else {
                    println("none")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a cascade with a condition without a subject passes`() {
        val code = """
            fun foo() = true

            fun test(i: Int) {
                if (i == 1) {
                    "a"
                } else if (i in listOf(2, 3, 4)) {
                    "b"
                } else if (foo()) {
                    "c"
                } else {
                    ""
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a cascade of plain boolean conditions passes`() {
        val code = """
            fun foo(a: Boolean, b: Boolean, c: Boolean) {
                if (a) {
                    println("a")
                }
                else if (b && c) {
                    println("b")
                }
                else if (!c) {
                    println("c")
                }
                else {
                    println("none")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a cascade on different subjects passes`() {
        val code = """
            fun test(i: Int, j: Int) {
                if (i == 1) {
                    println("a")
                } else if (j == 2) {
                    println("b")
                } else {
                    println("none")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with a nested if in a branch passes`() {
        val code = """
            fun foo(i: Int, b: Boolean) {
                if (i == 1) {
                    if (b) println("ab")
                    else println("a")
                }
                else if (i == 2) {
                    println("b")
                }
                else {
                    println("none")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an if with only one else if passes`() {
        val code = """
            fun test(i: Int) {
                if (i == 1) {
                    println("a")
                }
                else if (i == 2) {
                    println("b")
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a one-line cascade passes`() {
        val code = """
            fun foo(i: Int): Int {
                return if (i == 1) 42 else if (i == 2) 13 else 0
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a cascade with a break passes`() {
        val code = """
            fun foo(size: Int, j: Int) {
                for (i in 1..size) {
                    if (j == 1) {
                        break
                    }
                    else if (j == 2) {
                        println(i)
                    }
                    else {
                        println("*")
                    }
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
