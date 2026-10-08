package by.overpas.detekt.intellij.kotlin.redundantconstructs

import dev.detekt.api.Config
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class RedundantGetterTest {

    private val sut = RedundantGetter(Config.empty)

    @Test
    fun `a default getter is reported`() {
        val code = """
            class Test {
                val x = 1
                    get
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter that returns the field is reported`() {
        val code = """
            class Test {
                val x = 1
                    get() = field
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with only a field return in a block is reported`() {
        val code = """
            class Test {
                val x = 1
                    get() {
                        return field
                    }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter with an explicit type is reported`() {
        val code = """
            interface Test {
                val foo: Int
                    get(): Int
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an annotated getter that returns the field has a redundant body`() {
        val code = """
            class Foo {
                val foo: String = ""
                    @Deprecated("") get() {
                        return field
                    }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an annotated default getter passes`() {
        val code = """
            annotation class Inject

            class Test {
                val x = 1
                    @Inject get
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an external getter passes`() {
        val code = """
            class Foo {
                val foo: String
                    external get
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with a comment passes`() {
        val code = """
            class Test {
                val x = 1
                get() {
                    // comment
                    return field
                }
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter that does not return the field passes`() {
        val code = """
            class Test {
                val x: Int get() = 10
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with more statements passes`() {
        val code = """
            class Test {
                val x = 1
                    get() {
                        foo()
                        return field
                    }

                fun foo() {}
            }
        """.trimIndent()

        val findings = sut.lint(code)

        assertEquals(0, findings.size)
    }
}
